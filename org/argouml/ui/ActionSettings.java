// $Id$
// Copyright (c) 1996-2004 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.ui;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.argouml.i18n.Translator;
import org.argouml.application.api.Argo;
import org.argouml.application.api.PluggableSettingsTab;
import org.argouml.application.api.SettingsTabPanel;
import org.argouml.application.events.ArgoModuleEvent;
import org.argouml.application.events.ArgoModuleEventListener;
import org.argouml.uml.ui.UMLAction;

/**
 * Action object for handling Argo settings
 *
 * @author Thomas N
 * @author Thierry Lach
 * @since  0.9.4
 */
public class ActionSettings extends UMLAction
    implements ArgoModuleEventListener 
{

    ////////////////////////////////////////////////////////////////
    // static variables

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(Translator.class);

    /** 
     * One and only instance.
     *
     * @deprecated by Linus Tolke as of 0.17.1.
     *             Create your own instance of this action.
     */
    private static final ActionSettings SINGLETON = new ActionSettings();

    /**
     * Get the instance.
     *
     * @return The instance.
     * @deprecated by Linus Tolke as of 0.17.1.
     *             Create your own instance of this action.
     */
    public static ActionSettings getInstance() {
        return SINGLETON;
    }

    ////////////////////////////////////////////////////////////////
    // constructors
    private JButton applyButton = null;
    private JTabbedPane tabs = null;
    private ArgoDialog dialog = null;

    /**
     * Constructor.
     */
    public ActionSettings() {
        super("action.settings", HAS_ICON);
    }

    /**
     * Helper for localization.
     *
     * @param key The key to localize.
     * @return The localized String.
     */
    protected String localize(String key) {
        return Translator.localize("CoreSettings", key);
    }

    ////////////////////////////////////////////////////////////////
    // main methods

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
    	Object source = event.getSource();
    
        if (source instanceof JMenuItem) {
            ProjectBrowser pb = ProjectBrowser.getInstance();
            if (dialog == null) {
                try {
                    dialog = new ArgoDialog(pb, localize("dialog.settings"), 
					 ArgoDialog.OK_CANCEL_OPTION, true)
			{
			    public void actionPerformed(ActionEvent ev) {
				super.actionPerformed(ev);
				if (ev.getSource() == getOkButton()) {
				    handleSave();
				}
				else if (ev.getSource() == getCancelButton())
				{
				    handleCancel();
				}
			    }
			};

                    tabs = new JTabbedPane();

                    applyButton = new JButton(localize("button.apply"));
                    String mnemonic = localize("button.apply.mnemonic");
                    if (mnemonic != null && mnemonic.length() > 0) {
                        applyButton.setMnemonic(mnemonic.charAt(0));
                    }
                    applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    handleSave();
			}
		    });
                    dialog.addButton(applyButton);

                    ArrayList list =
                        Argo.getPlugins(PluggableSettingsTab.class);
                    ListIterator iterator = list.listIterator();
                    while (iterator.hasNext()) {
                        Object o = iterator.next();
                        SettingsTabPanel stp =
                            ((PluggableSettingsTab) o).getSettingsTabPanel();

                        tabs.addTab(
                                Translator.localize(
						  stp.getTabResourceBundleKey(),
						  stp.getTabKey()),
				    stp.getTabPanel());
                    }

                    // Increase width to accommodate all tabs on one row.
                    // (temporary solution until tabs are replaced with tree)
                    final int minimumWidth = 465;
                    tabs.setPreferredSize(
                            new Dimension(Math.max(tabs
						   .getPreferredSize().width,
						   minimumWidth),
					  tabs.getPreferredSize().height));

                    dialog.setContent(tabs);        
                }
                catch (Exception exception) {
                    LOG.error("got an Exception in ActionSettings", exception);
                }
            }
            
            handleRefresh();
            dialog.toFront();
            dialog.setVisible(true);
	}
    }

    /**
     * @see org.argouml.application.events.ArgoModuleEventListener#moduleLoaded(org.argouml.application.events.ArgoModuleEvent)
     */
    public void moduleLoaded(ArgoModuleEvent event) {
    }

    /**
     * @see org.argouml.application.events.ArgoModuleEventListener#moduleUnloaded(org.argouml.application.events.ArgoModuleEvent)
     */
    public void moduleUnloaded(ArgoModuleEvent event) {
    }

    /**
     * @see org.argouml.application.events.ArgoModuleEventListener#moduleEnabled(org.argouml.application.events.ArgoModuleEvent)
     */
    public void moduleEnabled(ArgoModuleEvent event) {
    }

    /**
     * @see org.argouml.application.events.ArgoModuleEventListener#moduleDisabled(org.argouml.application.events.ArgoModuleEvent)
     */
    public void moduleDisabled(ArgoModuleEvent event) {
    }

    private void handleSave() {
        for (int i = 0; i < tabs.getComponentCount(); i++) {
            Object o = tabs.getComponent(i);
            if (o instanceof SettingsTabPanel) {
                ((SettingsTabPanel) o).handleSettingsTabSave();
            }
        }
    }

    private void handleCancel() {
        for (int i = 0; i < tabs.getComponentCount(); i++) {
            Object o = tabs.getComponent(i);
            if (o instanceof SettingsTabPanel) {
                ((SettingsTabPanel) o).handleSettingsTabCancel();
            }
        }
    }

    private void handleRefresh() {
        for (int i = 0; i < tabs.getComponentCount(); i++) {
            Object o = tabs.getComponent(i);
            if (o instanceof SettingsTabPanel) {
                ((SettingsTabPanel) o).handleSettingsTabRefresh();
            }
        }
    }
}
/* end class ActionSettings */

