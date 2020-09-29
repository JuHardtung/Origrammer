package origrammer;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class AboutDialog extends JDialog {

	private JPanel jContentPane = null;
	
	public AboutDialog(JFrame frame, MainScreen __screen) {
		super(frame);
		init();
	}
	
	private void init() {
		this.setSize(400, 150);
		this.setContentPane(getJContentPane());
		this.setTitle("About");
	}
	
	private JPanel getJContentPane() {
		
		if (jContentPane == null) {
			JLabel versionLabel = new JLabel("Origrammer Version:");
			JLabel versionNrLabel = new JLabel(" 2.0.1");
			JLabel repoLabel = new JLabel("Repository : ");
			JLabel githubLabel = new JLabel("<html><a href=\\\"\\\">https://github.com/JuHardtung/Origrammer</a></html>");
			githubLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			JLabel developedByLabel = new JLabel("Developed by:");
			JLabel developerLabel = new JLabel("Julian Hardtung");
			JLabel mailLabel = new JLabel("E-Mail:");
			JLabel mailAdressLabel = new JLabel("<html><a href=\"\">ju.hardtung@gmx.de</a></html>");
			mailAdressLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			goWebsite(githubLabel);
			sendMail(mailAdressLabel);
			jContentPane = new JPanel();
			jContentPane.add(versionLabel);
			jContentPane.add(versionNrLabel);
			jContentPane.add(repoLabel);
			jContentPane.add(githubLabel);
			jContentPane.add(developedByLabel);
			jContentPane.add(developerLabel);
			jContentPane.add(mailLabel);
			jContentPane.add(mailAdressLabel);
			SpringLayout layout = new SpringLayout();
			jContentPane.setLayout(layout);
			SpringUtilities.makeCompactGrid(jContentPane, 4, 2, 6, 6, 6, 6);
		}
		
		return jContentPane;
	}
	
	private void goWebsite(JLabel githubLabel) {
		githubLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("https://github.com/JuHardtung/Origrammer"));
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}

			}
		});
	}
	
	private void sendMail(JLabel mailLabel) {
		mailLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().mail(new URI("mailto:ju.hardtung@gmx.de?subject=Origrammer"));
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}

			}
		});
	}
}
