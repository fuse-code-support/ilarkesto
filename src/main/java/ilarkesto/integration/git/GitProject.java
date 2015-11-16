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
package ilarkesto.integration.git;

import ilarkesto.core.base.Args;
import ilarkesto.scm.AScmProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GitProject extends AScmProject {

	public GitProject(Git tool, File dir) {
		super(tool, dir);
	}

	public boolean isInitialized() {
		return getGitDir().exists();
	}

	public File getGitDir() {
		return new File(getDir().getPath() + "/.git");
	}

	@Override
	public boolean isDirty() {
		String output = exec("status");
		return !output.contains("nothing to commit (working directory clean)");
	}

	@Override
	public String getLogAndDiffSince(String version) {
		throw new RuntimeException("Not implemented yet.");
	}

	@Override
	public String getVersion() {
		throw new RuntimeException("Not implemented yet.");
	}

	public boolean pull(String remote) {
		String output = remote == null ? exec("pull") : exec("pull", remote);
		output = output.trim();
		return !output.equals("Already up-to-date.");
	}

	@Override
	public boolean pullFromOrigin() {
		return pull(null);
	}

	private synchronized String exec(String... parameters) {
		return getTool().exec(getDir(), parameters);
	}

	@Override
	public Git getTool() {
		return (Git) super.getTool();
	}

	public void add(List<File> files) {
		List<String> params = new ArrayList<String>(files.size() + 1);
		params.add("add");
		for (File file : files) {
			params.add(file.getAbsolutePath());
		}
		getTool().exec(getDir(), params.toArray(new String[params.size()]));
	}

	public void addAll() {
		getTool().exec(getDir(), "add", "--all");
	}

	public void commit(String comment) {
		Args.assertNotNull(comment, "comment");
		getTool().exec(getDir(), "commit", "-m", comment);
	}

	public String status() {
		return exec("status");
	}

	public void deleteIndexLock() {
		new File(getGitDir().getPath() + "/index.lock").delete();
	}

}
