package com.dmcelligott.reminder;

import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class AppMain {

	private static boolean notificationsEnabled = true;
	private static long notificationPeriod = Constants.HOUR_MS;
	private static Timer timer;
	private static TimerTask reminderTask;
	private static TrayIcon trayIcon;

	public static void main(String[] args) {
		/* Use an appropriate Look and Feel */
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			// UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		/* Turn off metal's use of bold fonts */
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		// Schedule a job for the event-dispatching thread:
		// adding TrayIcon.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	protected static void createAndShowGUI() {

		if (SystemTray.isSupported()) {
			final SystemTray systemTray = SystemTray.getSystemTray();
			final Image defaultTrayIcon = createImage(
					"/images/sprint-white.png", "omt");
			final Image disabledTrayIcon = createImage(
					"/images/sprint-red.png", "omt");
			final Image notficationTrayIcon = createImage(
					"/images/sprint-green.png", "omt");

			trayIcon = new TrayIcon(defaultTrayIcon, "Reminder");
			trayIcon.setImageAutoSize(true);// Autosize icon base on space

			PopupMenu popup = new PopupMenu();

			MenuItem aboutItem = new MenuItem("About");
			CheckboxMenuItem enabledCheckBox = new CheckboxMenuItem("Enabled");
			MenuItem exitItem = new MenuItem("Exit");

			popup.add(aboutItem);

			popup.addSeparator();
			popup.add(createIntervalMenu());

			popup.add(enabledCheckBox);

			popup.addSeparator();
			popup.add(exitItem);

			trayIcon.setPopupMenu(popup);
			enabledCheckBox.setState(true);

			aboutItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane
							.showMessageDialog(
									null,
									"Saving you years of your life with a reminder to get up every once in a while\n\nDavid McElligott\ncontact: damc.dev@gmail.com");
				}
			});

			exitItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					systemTray.remove(trayIcon);
					System.exit(0);
				}
			});

			enabledCheckBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					int enabledStateChange = e.getStateChange();

					if (enabledStateChange == ItemEvent.SELECTED) {
						notificationsEnabled = true;
						trayIcon.setImage(defaultTrayIcon);
					} else {
						notificationsEnabled = false;
						trayIcon.setImage(disabledTrayIcon);
					}
				}
			});

			try {
				systemTray.add(trayIcon);
			} catch (Exception e) {
				e.printStackTrace();
			}

			timer = new Timer();
			reminderTask = createNewReminderTask();
			// schedule the task to run starting now and then every hour...
			timer.schedule(reminderTask, 0l, Constants.HOUR_MS);
		}

	}

	private static Menu createIntervalMenu() {
		Menu intervalOptions = new Menu("Interval");
		MenuItem hourIntervalItem = new MenuItem(Constants.LABEL_HOUR_INTERVAL);
		MenuItem twoHourIntervalItem = new MenuItem(
				Constants.LABEL_2_HOUR_INTERVAL);
		MenuItem threeHourIntervalItem = new MenuItem(
				Constants.LABEL_3_HOUR_INTERVAL);

		intervalOptions.add(hourIntervalItem);
		intervalOptions.add(twoHourIntervalItem);
		intervalOptions.add(threeHourIntervalItem);

		MenuItem thirtySecondIntervalItem = new MenuItem(
				Constants.LABEL_30_SECOND_INTERVAL);
		intervalOptions.add(thirtySecondIntervalItem);

		ActionListener intervalListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				MenuItem item = (MenuItem) e.getSource();
				String selectedInterval = item.getLabel();
				System.out.println(selectedInterval);

				if (selectedInterval.equals(Constants.LABEL_HOUR_INTERVAL)) {
					notificationPeriod = Constants.HOUR_MS;
				} else if (selectedInterval
						.equals(Constants.LABEL_2_HOUR_INTERVAL)) {
					notificationPeriod = Constants.HOUR_MS * 2;
				} else if (selectedInterval
						.equals(Constants.LABEL_3_HOUR_INTERVAL)) {
					notificationPeriod = Constants.HOUR_MS * 3;
				} else if (selectedInterval
						.equals(Constants.LABEL_30_SECOND_INTERVAL)) {
					notificationPeriod = Constants.MINUTE_MS / 2;
				}
				reminderTask.cancel();
				reminderTask = createNewReminderTask();
				timer.purge();
				timer.schedule(reminderTask, 0l, notificationPeriod);

			}

		};

		hourIntervalItem.addActionListener(intervalListener);
		twoHourIntervalItem.addActionListener(intervalListener);
		threeHourIntervalItem.addActionListener(intervalListener);
		thirtySecondIntervalItem.addActionListener(intervalListener);

		return intervalOptions;
	}

	protected static Image createImage(String path, String description) {
		URL imageURL = AppMain.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}

	private static TimerTask createNewReminderTask() {
		return new TimerTask() {
			@Override
			public void run() {
				System.out.println(new Date() + " [" + Thread.currentThread().toString() + "] You should get up and take a break!");
				if (notificationsEnabled == true) {
					trayIcon.displayMessage("Attention",
							"You should get up and take a break!",
							TrayIcon.MessageType.WARNING);
				}
			}
		};
	}
}
