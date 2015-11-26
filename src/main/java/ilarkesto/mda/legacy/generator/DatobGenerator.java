/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.mda.legacy.generator;

import ilarkesto.auth.AuthUser;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.core.money.Money;
import ilarkesto.core.persistance.EntityDoesNotExistException;
import ilarkesto.core.persistance.Persistence;
import ilarkesto.core.persistance.UniqueFieldConstraintException;
import ilarkesto.core.search.SearchText;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.DateRange;
import ilarkesto.core.time.DayAndMonth;
import ilarkesto.core.time.Time;
import ilarkesto.email.EmailAddress;
import ilarkesto.mda.legacy.model.ACollectionPropertyModel;
import ilarkesto.mda.legacy.model.BeanModel;
import ilarkesto.mda.legacy.model.DatobModel;
import ilarkesto.mda.legacy.model.EntityModel;
import ilarkesto.mda.legacy.model.PropertyModel;
import ilarkesto.mda.legacy.model.ReferenceListPropertyModel;
import ilarkesto.mda.legacy.model.ReferencePropertyModel;
import ilarkesto.mda.legacy.model.ReferenceSetPropertyModel;
import ilarkesto.mda.legacy.model.StringPropertyModel;
import ilarkesto.persistence.ADatob;
import ilarkesto.persistence.AEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DatobGenerator<D extends DatobModel> extends ABeanGenerator<D> {

	public static String persistenceUtilClass = Persistence.class.getName();

	public DatobGenerator(D bean) {
		super(bean);
	}

	protected boolean isCopyConstructorEnabled() {
		return true;
	}

	@Override
	protected void writeContent() {

		// nl();
		// comment("default constructor");
		// s(" public G").s(bean.getName()).s("() {").nl();
		// s(" super(null)").nl();
		// s(" }").nl();

		ln();
		ln("    private static final " + Log.class.getName() + " LOG = " + Log.class.getName() + ".get(" + getName()
				+ ".class);");

		if (!bean.isAbstract()) {
			ln();
			ln("    public static final String TYPE = \"" + bean.getName() + "\";");

			// ln();
			// comment("icon");
			// ln(" public String getIcon() {");
			// ln(" return TYPE;");
			// ln(" };");
		}

		if (isCopyConstructorEnabled()) writeCopyConstructor();

		if (bean.isSearchable()) {
			writeSearchable();
		}

		for (PropertyModel property : bean.getProperties()) {
			writeProperty(property);
		}

		BeanModel superbean = bean.getSuperbean();
		if (superbean != null && superbean instanceof DatobModel) {
			for (PropertyModel property : ((DatobModel) superbean).getProperties()) {
				writeSuperbeanProperty(property);
			}
		}

		ln();
		ln("    public void updateProperties(Map<String, String> properties) {");
		ln("        super.updateProperties(properties);");
		ln("        for (Map.Entry<String, String> entry : properties.entrySet()) {");
		ln("            String property = entry.getKey();");
		ln("            if (property.equals(\"id\")) continue;");
		ln("            String value = entry.getValue();");
		for (PropertyModel p : bean.getProperties()) {

			if (p.isValueObject()) {
				comment("TODO ValueObject");
				continue;
			}

			if (p.getType().equals(EmailAddress.class.getName())) {
				comment("TODO EmailAddress");
				continue;
			}

			if (p.getName().equals("changeNotificationEmails")) {
				comment("TODO changeNotificationEmails");
				continue;
			}

			String propertyName = p.getName();
			if (p.isReference()) {
				propertyName += "Id";
				if (p.isCollection()) propertyName += "s";
			}
			String parseType;
			if (p.isReference()) {
				if (p.isCollection()) {
					if (p instanceof ReferenceListPropertyModel) {
						parseType = "ReferenceList";
					} else {
						parseType = "ReferenceSet";
					}
				} else {
					parseType = "Reference";
				}
			} else {
				if (p.isCollection()) {
					parseType = p.getContentType() + "Collection";
					if (parseType.contains(".")) parseType = parseType.substring(parseType.lastIndexOf('.') + 1);
				} else {
					parseType = p.getType().substring(p.getType().lastIndexOf('.') + 1);
				}
			}
			ln("            if (property.equals(\"" + propertyName + "\")) update"
					+ Str.uppercaseFirstLetter(propertyName) + "(" + persistenceUtilClass + ".parseProperty"
					+ parseType + "(value));");
		}
		ln("        }");
		ln("    }");

		if (isLegacyBean(bean)) writeRepairDeadReferences();

		writeEnsureIntegrity();

		super.writeContent();
	}

	protected void writeEnsureIntegrity() {
		if (!(bean instanceof EntityModel)) return;
		ln();
		comment("ensure integrity");
		annotationOverride();
		s("    public void onEnsureIntegrity() {").ln();
		s("        super.onEnsureIntegrity();").ln();
		writeEnsureIntegrityContent();
		s("    }").ln();
	}

	protected void writeEnsureIntegrityContent() {
		for (PropertyModel p : bean.getProperties()) {
			if (p.isCollection()) {
				s("        if (").s(getFieldName(p)).s(" == null) ").s(getFieldName(p)).s(" = new ").s(getFieldImpl(p))
						.s("();").ln();
				if (p.isValueObject()) {
					s("        ensureIntegrityOfStructures(").s(getFieldName(p)).s(");").ln();
				}
				if (p.isReference()) {
					ln("        Set<String> " + p.getName() + " = new HashSet<String>(" + getFieldName(p) + ");");
					ln("        for (String entityId : " + p.getName() + ") {");
					ln("            try {");
					ln("                AEntity.getById(entityId);");
					ln("            } catch (" + EntityDoesNotExistException.class.getName() + " ex) {");
					ln("                LOG.info(\"Repairing dead " + p.getNameSingular() + " reference\");");
					ln("                repairDead" + Str.uppercaseFirstLetter(p.getNameSingular())
							+ "Reference(entityId);");
					ln("            }");
					ln("        }");

				}
			} else {
				if (p.isReference()) {
					ReferencePropertyModel pRef = (ReferencePropertyModel) p;
					if (pRef.isMaster()) {
						ln("        if (!is" + Str.uppercaseFirstLetter(p.getNameSingular()) + "Set()) {");
						ln("            repairMissingMaster();");
						ln("        }");
					}
					ln("        try {");
					ln("            get" + Str.uppercaseFirstLetter(p.getName()) + "();");
					ln("        } catch (" + EntityDoesNotExistException.class.getName() + " ex) {");
					ln("            LOG.info(\"Repairing dead " + p.getNameSingular() + " reference\");");
					ln("            repairDead" + Str.uppercaseFirstLetter(p.getNameSingular()) + "Reference("
							+ getFieldName(p) + ");");
					ln("        }");
				}
			}

		}
	}

	private void writeCopyConstructor() {
		ln();
		comment("copy constructor");
		s("    public G").s(bean.getName()).s("(G").s(bean.getName()).s(" template) {").ln();
		s("        super(template);").ln();
		s("        if (template==null) return;").ln().ln();
		for (PropertyModel p : bean.getProperties()) {
			String getterMethodPrefix = p.isBoolean() ? "is" : "get";
			s("        set").sU(p.getName()).s("(template.").s(getterMethodPrefix).sU(p.getName()).s("());").ln();
		}
		s("    }").ln();
	}

	private void writeRepairDeadReferences() {
		if (!bean.getProperties().isEmpty()) {
			ln();
			ln("    protected void repairDeadReferences(String entityId) {");
			ln("        if (!isPersisted()) return;");
			ln("        super.repairDeadReferences(entityId);");
			for (PropertyModel p : bean.getProperties()) {
				if (p.isCollection()) {
					s("        if (").s(getFieldName(p)).s(" == null) ").s(getFieldName(p)).s(" = new ")
							.s(getFieldImpl(p)).s("();").ln();
				}
				if (p.isValueObject()) {
					if (p.isCollection()) {
						// ln("        for(ADatob adatob : "+getFieldName(p)+") adatob.repairDeadReferences(entityId);");
						s("        repairDeadReferencesOfValueObjects(").s(getFieldName(p)).s(",entityId);").ln();
					} else {
						s("        if (").s(getFieldName(p)).s(" != null) ").s(getFieldName(p))
								.s(".repairDeadReferences(entityId);");
					}
				}
				if (!p.isReference()) continue;
				String nameUpper = Str.uppercaseFirstLetter(p.getNameSingular());
				s("        repairDead").s(nameUpper).s("Reference(entityId);").ln();
			}
			s("    }").ln();
		}
	}

	private void writeSearchable() {
		Set<PropertyModel> searchableProperties = bean.getSearchableProperties();
		if (searchableProperties.isEmpty()) return;

		ln();
		section("Searchable");

		ln();
		annotationOverride();
		ln("    public boolean matches(" + SearchText.class.getName() + " search) {");
		s("         return search.matches(");
		boolean first = true;
		for (PropertyModel p : searchableProperties) {
			if (first) {
				first = false;
			} else {
				s(", ");
			}
			s("get" + Str.uppercaseFirstLetter(p.getName()) + "()");
		}
		ln(");");
		ln("    }");

	}

	private void writeProperty(PropertyModel p) {
		String pNameUpper = Str.uppercaseFirstLetter(p.getName());

		ln("    // -----------------------------------------------------------");
		ln("    // - " + p.getName());
		ln("    // -----------------------------------------------------------");

		// --- property ---
		if (!isOutsourced(p)) {
			ln();
			s("    private " + getFieldType(p) + " " + getFieldName(p).substring(5));
			if (p.isCollection()) {
				s(" = new " + getFieldImpl(p) + "()");
			}
			ln(";");
		}

		String getterMethodPrefix = p.isBoolean() ? "is" : "get";

		if (p.isReference()) {

			if (p.isCollection()) {
				// --- getXxxIds ---
				ln();
				ln("    public final Collection<String> " + getterMethodPrefix + pNameUpper + "Ids() {");
				ln("        return", Collections.class.getName(), ".unmodifiableCollection(" + getFieldName(p) + ");");
				ln("    }");
			} else {
				// --- getXxxId ---
				ln();
				ln("    public final String " + getterMethodPrefix + pNameUpper + "Id() {");
				ln("        return " + getFieldName(p) + ";");
				ln("    }");
			}

		}

		// --- getXxx ---
		ln();
		String type2 = p.getType();
		if (p instanceof ReferencePropertyModel)
			type2 = getBeanClass(((ReferencePropertyModel) p).getReferencedEntity());
		if (p instanceof ReferenceSetPropertyModel)
			type2 = ((ReferenceSetPropertyModel) p).getCollectionType() + "<"
					+ getBeanClass(((ReferenceSetPropertyModel) p).getReferencedEntity()) + ">";
		// if (!isLegacyBean(bean) && p.isCollection()) type2 = "List<" + p.getContentType() + ">";
		ln("    public final " + type2 + " " + getterMethodPrefix + pNameUpper + "() {");
		writeGetXxxContent(p);
		ln("    }");

		// --- setXxx ---
		ln();
		String type3 = p.getType();
		if (p.isCollection()) type3 = "Collection<" + p.getContentType() + ">";
		if (p instanceof ReferencePropertyModel)
			type3 = getBeanClass(((ReferencePropertyModel) p).getReferencedEntity());
		if (p instanceof ReferenceSetPropertyModel)
			type3 = "Collection<" + getBeanClass(((ReferenceSetPropertyModel) p).getReferencedEntity()) + ">";
		ln("    public final void set" + pNameUpper + "(" + type3 + " " + p.getName() + ") {");
		ln("        " + p.getName() + " = " + "prepare" + pNameUpper + "(" + p.getName() + ");");
		writeSetXxxContent(p);
		ln("    }");

		if (p.isOptionRestricted()) {
			String optionType = type3;
			if (optionType.equals(int.class.getName())) optionType = Integer.class.getName();
			ln();
			ln("    public abstract List<" + optionType + "> get" + Str.uppercaseFirstLetter(p.getName())
					+ "Options();");
		}

		if (p instanceof StringPropertyModel) {
			StringPropertyModel sp = (StringPropertyModel) p;
			if (sp.isTemplateAvailable()) {
				ln();
				ln("    public abstract String get" + Str.uppercaseFirstLetter(p.getName()) + "Template();");
			}
		}

		if (p.isReference()) {
			ln();
			if (p.isCollection()) {
				ln("    public final void set" + pNameUpper + "Ids(" + p.getCollectionType() + "<String> ids) {");
				ln("        if (Utl.equals(" + p.getName() + "Ids, ids)) return;");

				if (!isLegacyBean(bean)) {
					ln("        clear" + Str.uppercaseFirstLetter(p.getName()) + "BackReferenceCache(ids, "
							+ p.getName() + "Ids);");
				}

				ln("        " + p.getName() + "Ids = ids;");
				writeModified(p);
				ln("    }");
			}
			if (!p.isCollection()) {
				ln("    public final void set" + pNameUpper + "Id(String id) {");
				ln("        if (Utl.equals(" + p.getName() + "Id, id)) return;");

				if (!isLegacyBean(bean)) {
					ln("        clear" + Str.uppercaseFirstLetter(p.getName()) + "BackReferenceCache(id, "
							+ getFieldName(p) + ");");
				}

				ln("        " + getFieldName(p) + " = id;");
				writeModified(p);
				ln("    }");

			}

			if (!isLegacyBean(bean)) {
				ln();
				if (bean.isAbstract()) {
					ln("    protected abstract void clear" + Str.uppercaseFirstLetter(p.getName())
							+ "BackReferenceCache(String oldId, String newId);");
				} else {
					String type = p.isCollection() ? "Collection<String>" : "String";
					ln("    private void clear" + Str.uppercaseFirstLetter(p.getName()) + "BackReferenceCache(" + type
							+ " oldId, " + type + " newId) {");
					ln("        " + p.getName() + "BackReferencesCache.clear(oldId);");
					ln("        " + p.getName() + "BackReferencesCache.clear(newId);");
					ln("    }");
				}
			}
		}

		// --- updateXxx ---
		if (!p.isReference()) {
			ln();
			ln("    private final void update" + pNameUpper + "(" + type3 + " " + p.getName() + ") {");
			writeSetXxxContent(p);
			ln("    }");
		}

		if (p.isReference()) {
			ln();
			if (p.isCollection()) {
				ln("    private final void update" + pNameUpper + "Ids(" + p.getCollectionType() + "<String> ids) {");
				ln("        set" + pNameUpper + "Ids(ids);");
				ln("    }");
			}
			if (!p.isCollection()) {
				ln("    private final void update" + pNameUpper + "Id(String id) {");
				ln("        set" + pNameUpper + "Id(id);");
				ln("    }");
			}
		}

		ln();
		ln("    protected " + type3 + " prepare" + pNameUpper + "(" + type3 + " " + p.getName() + ") {");
		if (p.isString()) {
			ln("         " + p.getName() + " = Str.removeControlChars(" + p.getName() + ");");
		}
		ln("        return " + p.getName() + ";");
		ln("    }");

		if (p.isCollection()) {
			writeCollectionProperty(p);
		} else {
			writeSimpleProperty(p);
		}

	}

	public boolean isOutsourced(PropertyModel p) {
		return p instanceof StringPropertyModel && ((StringPropertyModel) p).isOutsourced();
	}

	private void writeSuperbeanProperty(PropertyModel p) {
		if (!isLegacyBean(bean)) {
			if (p.isReference()) {
				ln();
				ln("    protected final void clear" + Str.uppercaseFirstLetter(p.getName())
						+ "BackReferenceCache(String oldId, String newId) {");
				ln("        " + p.getName() + "BackReferencesCache.clear(oldId);");
				ln("        " + p.getName() + "BackReferencesCache.clear(newId);");
				ln("    }");
			}
		}
	}

	private void writeModified(PropertyModel p) {
		ln("            updateLastModified();");
		if (p.isModified()) {
			String fieldName = getFieldName(p);
			ln("            fireModified(\"" + Str.removePrefix(fieldName, "this.") + "\", " + persistenceUtilClass
					+ ".propertyAsString(" + (isOutsourced(p) ? p.getName() : fieldName) + "));");
		}
	}

	private void writeGetXxxContent(PropertyModel p) {
		if (p.isReference()) {
			if (p.isCollection()) {
				String suffix = (p.getCollectionType().contains("Set")) ? "AsSet" : "";
				ln("        try {");
				ln("            return (" + p.getCollectionType() + ") AEntity.getByIds" + suffix + "("
						+ getFieldName(p) + ");");
				ln("        } catch (" + EntityDoesNotExistException.class.getName() + " ex) {");
				ln("            throw ex.setCallerInfo(\"" + bean.getName() + "." + p.getName() + "\");");
				ln("        }");
			} else {
				ReferencePropertyModel refP = (ReferencePropertyModel) p;
				ln("        try {");
				ln("            return " + getFieldName(p) + " == null ? null : ("
						+ getBeanClass(refP.getReferencedEntity()) + ") AEntity.getById(" + getFieldName(p) + ");");
				ln("        } catch (" + EntityDoesNotExistException.class.getName() + " ex) {");
				ln("            throw ex.setCallerInfo(\"" + bean.getName() + "." + p.getName() + "\");");
				ln("        }");
			}
		} else {
			if (p.isCollection()) {
				ACollectionPropertyModel cP = (ACollectionPropertyModel) p;
				if (p.isValueObject()) {
					ln("        return new " + cP.getCollectionImpl() + "(" + p.getName() + ");");
				} else {
					ln("        return new " + getFieldImpl(p) + "(" + p.getName() + ");");
				}
			} else {
				if (isOutsourced(p)) {
					ln("        return " + Persistence.class.getName() + ".loadOutsourcedString(this, \"" + p.getName()
							+ "\");");
				} else {
					ln("        return " + p.getName() + ";");
				}
			}
		}
	}

	private void writeSetXxxContent(PropertyModel p) {
		String pNameUpper = Str.uppercaseFirstLetter(p.getName());
		if (p.isReference()) {
			if (p.isCollection()) {
				ln("        if (" + p.getName() + " == null) " + p.getName() + " = Collections.emptyList();");
				String prefix = "";
				if (!isLegacyBean(bean)) prefix = persistenceUtilClass + ".";
				String suffix = p instanceof ReferenceListPropertyModel ? "AsList" : "AsSet";
				ln("        " + p.getCollectionType() + "<String> ids = " + prefix + "getIds" + suffix + "("
						+ p.getName() + ");");
				ln("        set" + pNameUpper + "Ids(ids);");
			} else {
				ln("        if (is" + pNameUpper + "(" + p.getName() + ")) return;");
				ln("        set" + pNameUpper + "Id(" + p.getName(), "== null ? null :", p.getName() + ".getId());");
			}
		} else {
			if (p.isCollection()) {
				ln("        if (" + p.getName() + " == null) " + p.getName() + " = Collections.emptyList();");
				ln("        if (" + getFieldName(p) + ".equals(" + p.getName() + ")) return;");
				if (p.isValueObject()) {
					ln("        " + getFieldName(p) + " = cloneValueObjects(" + p.getName() + ");");
					ln("        ensureIntegrityOfStructures(" + getFieldName(p) + ");");
				} else {
					ln("        " + getFieldName(p) + " = new " + getFieldImpl(p) + "(" + p.getName() + ");");
				}
				writeModified(p);
			} else {
				if (!isOutsourced(p)) {
					ln("        if (is" + pNameUpper + "(" + p.getName() + ")) return;");
				}
				if (p.isMandatory() && !p.isPrimitive()) {
					ln("        if (" + p.getName()
							+ " == null) throw new IllegalArgumentException(\"Mandatory field can not be set to null: "
							+ p.getName() + "\");");
				}
				if (p.isUnique()) {
					String findExpression;
					if (isLegacyBean(bean)) {
						findExpression = "getDao().get" + bean.getName() + "By" + pNameUpper + "(" + p.getName() + ")";
					} else {
						findExpression = bean.getName() + ".getBy" + pNameUpper + "(" + p.getName() + ")";
					}
					ln("        if (" + p.getName() + " != null) {");
					ln("            Object existing =", findExpression + ";");
					ln("            if (existing != null && existing != this) throw new "
							+ UniqueFieldConstraintException.class.getName() + "(\"" + bean.getName() + "\" ,\""
							+ p.getName() + "\", " + p.getName() + ");");
					ln("        }");
				}
				if (isOutsourced(p)) {
					ln("        " + Persistence.class.getName() + ".saveOutsourcedString(this, \"" + p.getName()
							+ "\", " + p.getName() + ");");
				} else {
					if (p.isValueObject()) {
						ln("        " + getFieldName(p) + " = " + p.getName() + ".clone();");
						ln("        " + getFieldName(p) + ".bind(this);");
					} else {
						ln("        " + getFieldName(p) + " = " + p.getName() + ";");
					}
				}
				writeModified(p);
			}
		}
	}

	protected final String getFieldName(PropertyModel p) {
		if (p.isReference()) {
			if (p.isCollection()) {
				return "this." + p.getName() + "Ids";
			} else {
				return "this." + p.getName() + "Id";
			}
		} else {
			return "this." + p.getName();
		}
	}

	private String getFieldType(PropertyModel p) {
		if (p.isReference()) {
			if (p.isCollection()) {
				return p.getCollectionType() + "<String>";
			} else {
				return "String";
			}
		} else {
			return p.getType();
		}
	}

	private String getFieldImpl(PropertyModel p) {
		if (p.isReference()) {
			if (p.isCollection()) {
				return p.getCollectionImpl() + "<String>";
			} else {
				return getFieldType(p);
			}
		} else {
			if (p.isCollection()) {
				return p.getCollectionImpl() + "<" + p.getContentType() + ">";
			} else {
				return p.getType();
			}
		}
	}

	private String getImpl(PropertyModel p) {
		if (p.isReference()) {
			if (p.isCollection()) {
				return p.getCollectionImpl() + "<" + p.getContentType() + ">";
			} else {
				return getFieldType(p);
			}
		} else {
			if (p.isCollection()) {
				return p.getCollectionImpl() + "<" + p.getContentType() + ">";
			} else {
				return p.getType();
			}
		}
	}

	protected void writeCollectionProperty(PropertyModel p) {
		String pNameSingularUpper = Str.uppercaseFirstLetter(p.getNameSingular());
		String pNameUpper = Str.uppercaseFirstLetter(p.getName());
		String paramExpr = p.isReference() ? p.getNameSingular() + ".getId()" : p.getNameSingular();

		// --- repairDeadXxxReference ---
		if (p.isReference()) {
			ln();
			ln("    protected void repairDead" + pNameSingularUpper + "Reference(String entityId) {");
			ln("        if (!isPersisted()) return;");
			ln("        if (" + getFieldName(p) + " == null ) return;");
			ln("        if (" + getFieldName(p) + ".remove(entityId)) {");
			writeModified(p);
			ln("        }");
			ln("    }");
		}

		// --- containsXxx ---
		ln();
		String contentType = p.getContentType();
		if (p instanceof ReferenceSetPropertyModel)
			contentType = getBeanClass(((ReferenceSetPropertyModel) p).getReferencedEntity());
		ln("    public final boolean contains" + pNameSingularUpper + "(" + contentType + " " + p.getNameSingular()
				+ ") {");
		ln("        if (" + p.getNameSingular() + " == null) return false;");
		ln("        if (" + getFieldName(p) + " == null) return false;");
		ln("        return " + getFieldName(p) + ".contains(" + paramExpr + ");");
		ln("    }");

		// --- getXxxCount ---
		ln();
		ln("    public final int get" + pNameUpper + "Count() {");
		ln("        if (" + getFieldName(p) + " == null) return 0;");
		ln("        return " + getFieldName(p) + ".size();");
		ln("    }");

		// --- isXxxEmpty ---
		ln();
		ln("    public final boolean is" + pNameUpper + "Empty() {");
		ln("        if (" + getFieldName(p) + " == null) return true;");
		ln("        return " + getFieldName(p) + ".isEmpty();");
		ln("    }");

		// --- addXxx ---
		ln();
		ln("    public final boolean add" + pNameSingularUpper + "(" + contentType + " " + p.getNameSingular() + ") {");
		ln("        if (" + p.getNameSingular() + " == null) throw new IllegalArgumentException(\""
				+ p.getNameSingular() + " == null\");");
		ln("        if (" + getFieldName(p) + " == null) " + getFieldName(p) + " = new " + getFieldImpl(p) + "();");
		if (p.isValueObject()) {
			ln("        boolean added = " + getFieldName(p) + ".add((" + contentType + ")" + paramExpr + ".clone());");
		} else {
			if (p instanceof ReferenceListPropertyModel) {
				if (!((ReferenceListPropertyModel) p).isDuplicatesAllowed()) {
					ln("        if (contains" + pNameSingularUpper + "(" + p.getNameSingular() + ")) return false;");
				}
			}
			ln("        boolean added = " + getFieldName(p) + ".add(" + paramExpr + ");");
			if (!isLegacyBean(bean) && p.isReference()) {
				ln("        if (added) " + p.getName() + "BackReferencesCache.clear(" + paramExpr + ");");
			}
		}
		if (p.isModified()) {
			ln("        if (added) {");
			if (p.isValueObject()) {
				ln("            ensureIntegrityOfStructures(" + getFieldName(p) + ");");
			}
			writeModified(p);
			ln("        }");
		}
		ln("        return added;");
		ln("    }");

		// --- addXxxs ---
		ln();
		ln("    public final boolean add" + pNameSingularUpper + "s(Collection<" + contentType + "> " + p.getName()
				+ ") {");
		ln("        if (" + p.getName() + " == null) throw new IllegalArgumentException(\"" + p.getName()
				+ " == null\");");
		ln("        if (" + getFieldName(p) + " == null) " + getFieldName(p) + " = new " + getFieldImpl(p) + "();");
		if (p.isValueObject()) {
			ln("        boolean added = false;");
			ln("        for (" + contentType + " " + p.getNameSingular() + " : " + p.getName() + ") {");

			ln("            added = added | " + getFieldName(p) + ".add((" + contentType + ")" + p.getNameSingular()
					+ ".clone());");
			ln("        }");
		} else {
			ln("        boolean added = false;");
			ln("        for (" + contentType + " " + p.getNameSingular() + " : " + p.getName() + ") {");
			if (p instanceof ReferenceListPropertyModel) {
				if (!((ReferenceListPropertyModel) p).isDuplicatesAllowed()) {
					ln("            if (" + getFieldName(p) + ".contains(" + p.getNameSingular()
							+ ".getId())) continue;");
				}
			}
			ln("            added = added | " + getFieldName(p) + ".add(" + paramExpr + ");");
			ln("        }");
		}
		if (!isLegacyBean(bean) && p.isReference()) {
			ln("        if (added) " + p.getName() + "BackReferencesCache.clear(" + getFieldName(p) + ");");
		}
		if (p.isModified()) {
			ln("        if (added) {");
			if (p.isValueObject()) {
				ln("            ensureIntegrityOfStructures(" + getFieldName(p) + ");");
			}
			writeModified(p);
			ln("        }");
		}
		ln("        return added;");
		ln("    }");

		// --- removeXxx ---
		ln();
		ln("    public final boolean remove" + pNameSingularUpper + "(" + contentType + " " + p.getNameSingular()
				+ ") {");
		ln("        if (" + p.getNameSingular() + " == null) return false;");
		ln("        if (" + getFieldName(p) + " == null) return false;");
		ln("        boolean removed = " + getFieldName(p) + ".remove(" + paramExpr + ");");
		if (!isLegacyBean(bean) && p.isReference()) {
			ln("        if (removed) " + p.getName() + "BackReferencesCache.clear(" + paramExpr + ");");
		}
		if (p.isModified()) {
			ln("        if (removed) {");
			writeModified(p);
			ln("        }");
		}
		ln("        return removed;");
		ln("    }");

		// --- removeXxxs ---
		ln();
		ln("    public final boolean remove" + pNameSingularUpper + "s(Collection<" + contentType + "> " + p.getName()
				+ ") {");
		ln("        if (" + p.getName() + " == null) return false;");
		ln("        if (" + p.getName() + ".isEmpty()) return false;");
		ln("        if (" + getFieldName(p) + " == null) return false;");
		ln("        boolean removed = false;");
		ln("        for (" + contentType + " _element: " + p.getName() + ") {");
		ln("            removed = removed | " + getFieldName(p) + ".remove(_element);");
		ln("        }");
		if (!isLegacyBean(bean) && p.isReference()) {
			ln("        if (removed) " + p.getName() + "BackReferencesCache.clear(" + getFieldName(p) + ");");
		}
		if (p.isModified()) {
			ln("        if (removed) {");
			writeModified(p);
			ln("        }");
		}
		ln("        return removed;");
		ln("    }");

		// --- clearXxx ---
		ln();
		ln("    public final boolean clear" + pNameUpper + "() {");
		ln("        if (" + getFieldName(p) + " == null) return false;");
		ln("        if (" + getFieldName(p) + ".isEmpty()) return false;");
		if (!isLegacyBean(bean) && p.isReference()) {
			ln("        " + p.getName() + "BackReferencesCache.clear(" + getFieldName(p) + ");");
		}
		ln("        " + getFieldName(p) + ".clear();");
		writeModified(p);
		ln("        return true;");
		ln("    }");

		if (contentType.equals(String.class.getName())) {
			// --- getXxxAsCommaSeperatedString
			ln();
			ln("    public final String get" + pNameUpper + "AsCommaSeparatedString() {");
			ln("        if (" + getFieldName(p) + " == null) return null;");
			ln("        if (" + getFieldName(p) + ".isEmpty()) return null;");
			ln("        return Str.concat(" + getFieldName(p) + ",\", \");");
			ln("    }");

			// --- setXxxAsCommaSeperatedString
			ln();
			ln("    public final void set" + pNameUpper + "AsCommaSeparatedString(String " + p.getName() + ") {");
			ln("        set" + pNameUpper + "(Str.parseCommaSeparatedString(" + p.getName() + "));");
			ln("    }");
		}

	}

	private void writeSimpleProperty(PropertyModel p) {
		String pNameUpper = Str.uppercaseFirstLetter(p.getName());

		// --- repairDeadXxxReference ---
		if (p.isReference()) {
			ReferencePropertyModel pRef = (ReferencePropertyModel) p;
			ln();
			ln("    protected void repairDead" + Str.uppercaseFirstLetter(p.getNameSingular())
					+ "Reference(String entityId) {");
			ln("        if (!isPersisted()) return;");
			ln("        if (" + getFieldName(p) + " == null || entityId.equals(" + getFieldName(p) + ")) {");
			if (pRef.isMaster()) {
				ln("            repairMissingMaster();");
			} else {
				ln("            set" + pNameUpper + "(null);");
			}
			ln("        }");
			ln("    }");
		}

		if (!p.isPrimitive() && !isOutsourced(p)) {
			// --- isXxxSet ---
			ln();
			ln("    public final boolean is" + pNameUpper + "Set() {");
			ln("        return " + getFieldName(p) + " != null;");
			ln("    }");
		}

		// --- isXxx ---
		if (!isOutsourced(p)) {
			ln();
			String type4 = p.getType();
			if (p instanceof ReferencePropertyModel)
				type4 = getBeanClass(((ReferencePropertyModel) p).getReferencedEntity());
			ln("    public final boolean is" + pNameUpper + "(" + type4 + " " + p.getName() + ") {");
			if (p.isPrimitive()) {
				ln("        return " + getFieldName(p) + " == " + p.getName() + ";");
			} else {
				ln("        if (" + getFieldName(p) + " == null && " + p.getName() + " == null) return true;");
				if (p.isReference()) {
					ln("        return " + p.getName() + " != null && " + p.getName() + ".getId().equals("
							+ getFieldName(p) + ");");
				} else {
					if (p.getType().equals(BigDecimal.class.getName())) {
						ln("        return " + getFieldName(p) + " != null && " + p.getName() + " != null && "
								+ getFieldName(p) + ".compareTo(" + p.getName() + ") == 0;");
					} else {
						ln("        return " + getFieldName(p) + " != null && " + getFieldName(p) + ".equals("
								+ p.getName() + ");");
					}
				}
			}
			ln("    }");
		}

		// --- updateXxx(Object value) ---
		ln();
		if (!p.isReference() || isLegacyBean(bean)) {
			ln("    protected final void update" + pNameUpper + "(Object value) {");
			if (p.isReference()) {
				if (isLegacyBean(bean)) {
					String daoExpr = p.getDaoName();
					if (p.isAbstract()) {
						daoExpr = "getDaoService()";
					}
					ln("        set" + pNameUpper + "(value == null ? null : (" + p.getType() + ")" + daoExpr
							+ ".getById((String)value));");
				} else {
					ln("        if (xxxUtl.equals(" + getFieldName(p) + ", value)) return;");
					ln("        " + getFieldName(p) + " = (String)value;");
					writeModified(p);
				}
			} else if (p.isPrimitive()) {
				String type = p.getType();
				if (type.equals("int")) type = Integer.class.getSimpleName();
				if (type.equals("boolean")) type = Boolean.class.getSimpleName();
				ln("        set" + pNameUpper + "((" + type + ")value);");
			} else {
				String type = p.getType();
				if (type.equals(Date.class.getName())) {
					ln("        value = value == null ? null : new " + Date.class.getName() + "((String)value);");
				} else if (type.equals(Time.class.getName())) {
					ln("        value = value == null ? null : new " + Time.class.getName() + "((String)value);");
				} else if (type.equals(DateAndTime.class.getName())) {
					ln("        value = value == null ? null : new " + DateAndTime.class.getName() + "((String)value);");
				} else if (type.equals(DateRange.class.getName())) {
					ln("        value = value == null ? null : new " + DateRange.class.getName() + "((String)value);");
				} else if (type.equals(DayAndMonth.class.getName())) {
					ln("        value = value == null ? null : new " + DayAndMonth.class.getName() + "((String)value);");
				} else if (type.equals(Money.class.getName())) {
					ln("        value = value == null ? null : new " + Money.class.getName() + "((String)value);");
				}
				ln("        set" + pNameUpper + "((" + p.getType() + ")value);");
			}
			ln("    }");
		}
	}

	protected String getBeanClass(BeanModel entity) {
		return entity.getBeanClass();
	}

	@Override
	protected String getSuperclass() {
		return bean.getSuperclass();
	}

	@Override
	protected Set<String> getImports() {
		Set<String> result = new LinkedHashSet<String>();
		result.addAll(super.getImports());
		if (isLegacyBean(bean)) {
			result.add(ADatob.class.getName());
			result.add(AEntity.class.getName());
			result.add(AuthUser.class.getName());
		}
		result.add(Str.class.getName());
		return result;
	}

	// --- dependencies ---

}
