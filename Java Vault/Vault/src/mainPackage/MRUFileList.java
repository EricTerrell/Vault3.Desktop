/*
  Vault 3
  (C) Copyright 2022, Eric Bergman-Terrell
  
  This file is part of Vault 3.

    Vault 3 is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Vault 3 is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Vault 3.  If not, see <http://www.gnu.org/licenses/>.
*/

package mainPackage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MRUFileList {
	public static class MRUFile {
		private String filePath;
		
		public String getFilePath() {
			return filePath;
		}
		
		public String getMenuText() {
			return new File(filePath).getName();
		}

		public MRUFile(String filePath, String password) {
			this.filePath = filePath;
		}
	}
	
	private List<MRUFile> mruFiles;
	
	public List<MRUFile> getMRUFiles() {
		return mruFiles;
	}
	
	public MRUFileList() {
		mruFiles = new ArrayList<>();
	}
	
	public void update(String filePath, String password) {
		MRUFile mruFile = new MRUFile(filePath, password);
		
		String fileName = new File(filePath).getName();
		
		for (int i = mruFiles.size() - 1; i >= 0; i--) {
			String currentFileName = new File(mruFiles.get(i).filePath).getName();
			
			if (fileName.compareToIgnoreCase(currentFileName) == 0) {
				mruFiles.remove(i);
			}
		}
		
		mruFiles.add(0, mruFile);
		
		Globals.getMainApplicationWindow().createFileMenuItems();
		
		save();
	}
	
	public void remove(String filePath) {
		String fileName = new File(filePath).getName();

		for (int i = mruFiles.size() - 1; i >= 0; i--) {
			String currentFileName = new File(mruFiles.get(i).filePath).getName();
			
			if (fileName.compareToIgnoreCase(currentFileName) == 0) {
				mruFiles.remove(i);
			}
		}
		
		save();
	}
	
	public void load() {
		int maxMRUFiles = Globals.getPreferenceStore().getInt(PreferenceKeys.MaxMRUFiles);

		for (int i = 0; i < maxMRUFiles; i++) {
			String name = String.format("%s%s", PreferenceKeys.MRUFile, i + 1);
			String filePath = Globals.getPreferenceStore().getString(name);

			if (filePath != null && filePath.length() > 0) {
				MRUFile mruFile = new MRUFile(filePath, null);
				mruFiles.add(mruFile);
			}
		}
	}
	
	private void save() {
		int maxMRUFiles = Globals.getPreferenceStore().getInt(PreferenceKeys.MaxMRUFiles);
		
		while (Globals.getMRUFiles().getMRUFiles().size() > maxMRUFiles) {
			Globals.getMRUFiles().getMRUFiles().remove(Globals.getMRUFiles().getMRUFiles().size() - 1);
		}

		for (int i = 0; i < maxMRUFiles; i++) {
			String name = String.format("%s%s", PreferenceKeys.MRUFile, i + 1);
			Globals.getPreferenceStore().setValue(name, StringLiterals.EmptyString);
		}
		
		for (int i = 0; i < Globals.getMRUFiles().getMRUFiles().size(); i++) {
			String name = String.format("%s%s", PreferenceKeys.MRUFile, i + 1);
			Globals.getPreferenceStore().setValue(name, Globals.getMRUFiles().getMRUFiles().get(i).getFilePath());
		}
	}
}
