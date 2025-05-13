package mainPackage;

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;

public class PropertyChangeListener implements IPropertyChangeListener {
	private PreferenceStore preferenceStore;
	
	public PropertyChangeListener(PreferenceStore preferenceStore) {
		this.preferenceStore = preferenceStore;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
		try {
			preferenceStore.save();
		} catch (IOException ex) {
			ex.printStackTrace();

			Globals.getLogger().info(String.format("close() - cannot save user preferences: %s", ex.getMessage()));

			final Image icon = Globals.getImageRegistry().get(Globals.IMAGE_REGISTRY_VAULT_ICON);

			final String message = MessageFormat.format(
					"Cannot save user preferences.{0}{0}{1}",
					PortabilityUtils.getNewLine(),
					ex.getMessage());

			final MessageDialog messageDialog = new MessageDialog(
					Globals.getMainApplicationWindow().getShell(),
					StringLiterals.ProgramName,
					icon,
					message,
					MessageDialog.ERROR,
					new String[] { "&Close" },
					0);

			messageDialog.open();
		}
	}

}
