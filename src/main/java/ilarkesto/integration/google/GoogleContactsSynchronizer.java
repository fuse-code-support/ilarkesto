/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ilarkesto.integration.google;

import ilarkesto.base.Utl;
import ilarkesto.core.auth.LoginData;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.integration.google.Google.AddressRel;
import ilarkesto.io.IO;
import ilarkesto.swing.LoginPanel;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactGroupEntry;

public class GoogleContactsSynchronizer {

	private static final Log log = Log.get(GoogleContactsSynchronizer.class);

	public static void main(String[] args) {
		LoginData clientIdLogin = LoginPanel.showDialog(null, "Client ID",
			new File("runtimedata/google-oauth2.properties"));
		if (clientIdLogin == null) return;

		LoginData refreshTokenLogin = LoginPanel.showDialog(null, "Refresh Token",
			new File("runtimedata/google-oauth2-refresh.properties"));

		GoogleOAuth client = new GoogleOAuth(clientIdLogin.getLogin(), clientIdLogin.getPassword(),
				GoogleOAuth.REDIRECT_OOB, refreshTokenLogin.getPassword(), GoogleOAuth.SCOPE_USERINFO_EMAIL,
				GoogleOAuth.SCOPE_CONTACTS);

		// System.out.println(client.createUrlForAuthenticationRequest(false));

		ContactsService contactsService = client.createContactsService();
		GoogleContactsSynchronizer synchronizer = new GoogleContactsSynchronizer(contactsService, "ilarkestoId",
				"ilarkestoTimestammp", "ilarkestoVersion", String.valueOf(System.currentTimeMillis()), "Ilarkesto-Test",
				false, new LocalContactManager<String>() {

					@Override
					public void onUpdateGoogleContactFailed(String contact, ContactEntry gContact, Exception ex) {
						throw new RuntimeException(ex);
					}

					@Override
					public File getContactPhoto(String contact) {
						return null;
					}

					@Override
					public void updateGoogleContactFields(String contact, ContactEntry gContact) {
						Google.setEmail(gContact, "test@test.com", null, Google.EmailRel.HOME, true);
						gContact.addOrganization(Google.createOrganization("Test GmbH", "Badass"));
						gContact.addUserDefinedField(
							Google.createUserDefinedField("Ilarkesto Test", "Ein\nZweizeiler"));
						gContact.setBirthday(Google.createBirthday(new Date(1979, 8, 3)));
						Google.setAddress(gContact, "Unter Franke 1", "31737", "Rinteln", "DE", "Deutschland", "",
							AddressRel.WORK, false);
					}

					@Override
					public String getContactById(String localId) {
						return localId;
					}

					@Override
					public Set<String> getContacts() {
						return Utl.toSet("Test1", "Test2");
					}

					@Override
					public String getId(String contact) {
						return contact;
					}

					@Override
					public DateAndTime getLastModified(String contact) {
						return new DateAndTime("2010-01-01 03:00:00");
					}
				});
		synchronizer.updateGoogle();

	}

	private ContactsService service;
	private String localIdentifierAttribute;
	private String localTimestampAttribute;
	private String localVersionAttribute;
	private String localVersion;
	private String contactsGroupTitle;
	private LocalContactManager localContactManager;
	private boolean addToMyContacts;
	private ContactGroupEntry groupMyContacts;

	public GoogleContactsSynchronizer(ContactsService service, String localIdentifierAttribute,
			String localTimestampAttribute, String localVersionAttribute, String localVersion,
			String contactsGroupTitle, boolean addToMyContacts, LocalContactManager localContactManager) {
		super();
		this.service = service;
		this.localIdentifierAttribute = localIdentifierAttribute;
		this.localTimestampAttribute = localTimestampAttribute;
		this.localVersionAttribute = localVersionAttribute;
		this.localVersion = localVersion;
		this.contactsGroupTitle = contactsGroupTitle;
		this.addToMyContacts = addToMyContacts;
		this.localContactManager = localContactManager;
	}

	public void updateGoogle() {
		ContactGroupEntry group = getContactGroup();

		Collection<ContactEntry> gContacts = Google.getContacts(service, group, null);
		Set oContacts = localContactManager.getContacts();

		for (ContactEntry gContact : gContacts) {
			String oContactId = Google.getExtendedProperty(gContact, localIdentifierAttribute);
			Object oContact = null;
			if (oContactId != null) {
				oContact = localContactManager.getContactById(oContactId);
			}

			if (oContact == null) {
				Google.delete(gContact);
				continue;
			}

			String ts = Google.getExtendedProperty(gContact, localTimestampAttribute);
			String version = Google.getExtendedProperty(gContact, localVersionAttribute);

			if (ts == null || !ts.equals(localContactManager.getLastModified(oContact).toString())
					|| !localVersion.equals(version)) {
				updateGoogleContact(oContact, gContact);
			}
			oContacts.remove(oContact);
		}

		for (Object oContact : oContacts) {
			ContactEntry gContact = Google.createContact(oContact.toString(), group, service, null);
			updateGoogleContact(oContact, gContact);
		}
	}

	private void updateGoogleContact(Object oContact, ContactEntry gContact) {
		try {
			updateGoogleContactInternal(oContact, gContact);
		} catch (Exception ex) {
			localContactManager.onUpdateGoogleContactFailed(oContact, gContact, ex);
		}
	}

	private synchronized void updateGoogleContactInternal(Object oContact, ContactEntry gContact) {
		log.info("Updating google contact:", oContact);
		Google.setExtendedProperty(gContact, localIdentifierAttribute, localContactManager.getId(oContact));
		Google.setExtendedProperty(gContact, localTimestampAttribute,
			localContactManager.getLastModified(oContact).toString());
		Google.setExtendedProperty(gContact, localVersionAttribute, localVersion);

		Google.removeEmails(gContact);
		Google.removePhones(gContact);
		Google.removeAddresses(gContact);
		Google.removeInstantMessages(gContact);
		Google.removeWebsites(gContact);
		Google.removeOrganizations(gContact);
		Google.removeUserDefinedFields(gContact);

		if (addToMyContacts) Google.addContactGroup(gContact, getGroupMyContacts());

		localContactManager.updateGoogleContactFields(oContact, gContact);

		Google.save(gContact, service);

		File photoFile = localContactManager.getContactPhoto(oContact);
		if (photoFile != null && photoFile.exists()) {
			byte[] photoData = IO.readFileToByteArray(photoFile);
			try {
				Google.uploadContactPhoto(gContact, service, "image/png", photoData);
			} catch (Exception ex) {
				log.error("Uploading photo file failed:", this, oContact, photoFile);
			}
		}
	}

	private ContactGroupEntry getGroupMyContacts() {
		if (groupMyContacts == null) groupMyContacts = Google.getContactGroupMyContacts(service, null);
		return groupMyContacts;
	}

	private ContactGroupEntry getContactGroup() {
		ContactGroupEntry group = Google.getContactGroupByTitle(contactsGroupTitle, service, null);
		if (group == null) {
			group = Google.createContactGroup(contactsGroupTitle, service, null);
		}
		return group;
	}

	public static interface LocalContactManager<C> {

		void onUpdateGoogleContactFailed(C contact, ContactEntry gContact, Exception ex);

		File getContactPhoto(C contact);

		void updateGoogleContactFields(C contact, ContactEntry gContact);

		C getContactById(String localId);

		Set<C> getContacts();

		String getId(C contact);

		DateAndTime getLastModified(C contact);

	}

}
