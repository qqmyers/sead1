/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dnd;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.awt.datatransfer.*;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.swing.border.Border;

/**
 * Demonstration of the top-level {@code TransferHandler}
 * support on {@code JFrame}.
 *
 * @author Shannon Hickey
 */
public class DragAndDropMedici extends JFrame {

    TrayIcon ti;
    private static final String BOUNDRY = "==================================";
    public static final int NOTIFICATION_ID = 1001;
    private JPanel panel = new JPanel();
//    private JPanel pnlLabels;// = new JPanel();
//    private JPanel pnlProgressBars;// = new JPanel();
    //private JProgressBar progressBar;
    static DragAndDropMedici dragDropWindow = null;
    GridLayout experimentLayout;
    final String UPLOADING_STRING = "Uploading...";
    static LoginForm loginForm = null;

    private Properties getProperties() {
        MediciPreferences mediciPreferences = MediciPreferences.getInstance();
        return mediciPreferences.getProperties();
//        try {
////            _properties.load(new FileInputStream("MediciPreferences.properties"));
//            _properties.load(new FileInputStream("MediciPreferences.properties"));
//
//        } catch (IOException ex) {
//            Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return _properties;
    }

    private class MediciFileHandler extends InternalFrameAdapter implements PropertyChangeListener {

        String name;
        TransferHandler th;
        private Task task;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
//            if ("progress" == evt.getPropertyName()) {
//                int progress = (Integer) evt.getNewValue();
//                try {
//                    progressBar.setValue(progress);
//
//                } catch (Exception ex) {
//                    String message = ex.getMessage();
//                    message = message;
//                }
//            }
        }
        JProgressBar progressBar;

        class Task extends SwingWorker<Void, Void> {

            InputStream _inputStream;
            String _fileName;
            String _mimeType;
            long _fileLength;

            private Task(InputStream inputStream, String fileName, String mimeType, long fileLength) {
                this._inputStream = inputStream;
                this._fileName = fileName;
                this._mimeType = mimeType;
                this._fileLength = fileLength;
            }

            private void upload() throws Exception {

                Properties properties = dragDropWindow.getProperties();
                if (properties == null) {
                    throw new AuthenticationException("Properties not loaded. No user name/ password found");
                }
                String username = properties.getProperty("user");
                String password = getDecryptedPassword(properties.getProperty("pass"));
                final String server = properties.getProperty("server");

                //AssetFileDescriptor asset = null;
                HttpURLConnection conn = null;
                try {
                    // Make a connect to the server
                    URL url = new URL(server + "UploadBlob");
                    conn = (HttpURLConnection) url.openConnection();

                    // Put the authentication details in the request
                    String up = username + ":" + password;

                    String encodedUsernamePassword = Base64.encodeToString(up.getBytes(), Base64.NO_WRAP);
                    conn.setRequestProperty("Authorization", "Basic " + encodedUsernamePassword);

                    // make it a post request
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");

                    // mark it as multipart
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDRY);

                    // create output stream
                    DataOutputStream dataOS = new DataOutputStream(conn.getOutputStream());

                    // write data
                    dataOS.writeBytes("--" + BOUNDRY + "\r\n");
                    dataOS.writeBytes("Content-Disposition: form-data; name=\"" + _fileName + "\"; filename=\"" + _fileName + "\";\r\n");

                    if (_mimeType != null) {
                        dataOS.writeBytes("Content-Type: " + _mimeType + "\r\n");
                    }
                    dataOS.writeBytes("\r\n");

                    // actual data to be written
                    byte[] buf = new byte[10240];
                    int len = 0;
                    int count = 0;
                    while ((len = _inputStream.read(buf)) > 0) {
                        count++;
                        if (count >= 10) {
                            count = 0;
                        }
                        dataOS.write(buf, 0, len);
                    }
                    _inputStream.close();

                    // write final boundary and done
                    dataOS.writeBytes("\r\n--" + BOUNDRY + "--");
                    dataOS.flush();
                    dataOS.close();

                    // Ensure we got the HTTP 200 response code
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 401) {
                        throw new AuthenticationException("Invalid username/password");
                    }
                    if (responseCode != 200) {
                        throw new Exception(String.format("Received the response code %d from the URL %s", responseCode, conn.getResponseMessage()));
                    }

                    // Read the response
                    _inputStream = conn.getInputStream();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = _inputStream.read(bytes)) != -1) {
                        baos.write(bytes, 0, bytesRead);
                    }
                    _inputStream.close();
                } catch (Exception ex) {
                    String message = ex.getMessage();
                    JOptionPane.showMessageDialog(dragDropWindow, message);
                    progressBar.setString("Error while uploading...");
                    DragAndDropMedici.showLoginForm();
                }
            }

            private String getDecryptedPassword(String encryptedPassword) {
                DesEncrypter encrypter = new DesEncrypter("MyP@$$w0rd");
                // Decrypt
                String decrypted = encrypter.decrypt(encryptedPassword);
                return decrypted;
            }
            /*
             * Main task. Executed in background thread.
             */

            @Override
            public Void doInBackground() {

                try {
                    upload();
                } catch (Exception ex) {
                }
                return null;
            }

            /*
             * Executed in event dispatching thread
             */
            @Override
            public void done() {
                Toolkit.getDefaultToolkit().beep();
                if (progressBar.getString().equals(UPLOADING_STRING)) {
                    progressBar.setString("Upload completed");
                }
                progressBar.setIndeterminate(false);
            }
        }

        public MediciFileHandler(File file) {
            this.name = file.getName();
            try {
//                if (pnlLabels == null || pnlProgressBars == null) {
//                    pnlLabels = new JPanel();
//                    pnlLabels.setLayout(new BoxLayout(pnlLabels, BoxLayout.Y_AXIS));
//                    pnlProgressBars = new JPanel();
//                    pnlProgressBars.setLayout(new BoxLayout(pnlProgressBars, BoxLayout.Y_AXIS));
//                    try {
//                        panel.add(pnlLabels, BorderLayout.NORTH);
//                        panel.add(pnlProgressBars, BorderLayout.NORTH);
//                    } catch (Exception ex) {
//                        String msg = ex.getMessage();
//
//                        System.out.println(msg);
//                    }
//
//                }

                JPanel horizontalPanel = new JPanel();
                horizontalPanel.setPreferredSize(new Dimension(200, 100));
                JLabel label = new JLabel(file.getName() + ": ");
                label.setPreferredSize(new Dimension(200, 50));
                //label.setSize(100, 50);
                progressBar = new JProgressBar(0, 100);
                progressBar.setString(UPLOADING_STRING);
                progressBar.setStringPainted(true);
                progressBar.setSize(75, 40);
                progressBar.setIndeterminate(true);

                horizontalPanel.setLayout(new BoxLayout(horizontalPanel, BoxLayout.X_AXIS));

                horizontalPanel.add(label);
                horizontalPanel.add(progressBar);
//                pnlLabels.add(label);
//                pnlProgressBars.add(progressBar);
                panel.add(horizontalPanel, BorderLayout.NORTH);

                if (scroller == null) {
                    scroller = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    dragDropWindow.add(scroller);
                }
                dragDropWindow.validate();
                init(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        public MediciFileHandler(String name) {
            this.name = name;
            init(getClass().getResource(name));
        }

        private void init(URL url) {
            try {
                InputStream inputStream = url.openStream();
                String encodedPath = url.getPath();
                String decodedFilePath = URLDecoder.decode(encodedPath, "UTF-8");

                long fileLength = new File(decodedFilePath).length();

                URLConnection uc = uc = url.openConnection();

                String mimeType = uc.getContentType();

                //Instances of javax.swing.SwingWorker are not reusuable, so
                //we create new instances as needed.
                task = new Task(inputStream, name, mimeType, fileLength);
                task.addPropertyChangeListener(this);
                task.execute();

                //upload(inputStream, filename, mimeType);

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        public String toString() {
            return name;
        }
    }
    private TransferHandler handler = new TransferHandler() {

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return false;
            }
            return true;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            Transferable t = support.getTransferable();

            try {
                java.util.List<File> droppedFilesList =
                        (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

                for (File file : droppedFilesList) {
                    new MediciFileHandler(file);
                }
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (IOException e) {
                return false;
            }

            return true;
        }
    };
    JScrollPane scroller;

    public DragAndDropMedici() {
        super("Drop files to upload");
        //setResizable(false);
    }

    private void createAndShowGUI(String[] args) {

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setTransferHandler(handler);
        panel.setAutoscrolls(true);

        dragDropWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int width = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width / 2;
        dragDropWindow.setSize(width, 200);
        dragDropWindow.add(panel);
        //dragDropWindow.add(scroller, BorderLayout.CENTER);

        dragDropWindow.setLocationRelativeTo(null);
        dragDropWindow.validate();
        dragDropWindow.setVisible(true);

        Properties properties = getProperties();

        String userName = properties.getProperty("user");
        String password = properties.getProperty("pass");

        if (userName.equals("") || password.equals("")) {

            showLoginForm();
        }

        if (args.length > 0) {
            for (int index = 0; index < args.length; index++) {
                File file = new File(args[index]);
                new MediciFileHandler(file);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DragAndDropMedici.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //showSystemTrayIcon();
    }

    private static void showLoginForm() {

        if (loginForm == null) {
            loginForm = new LoginForm();
        }
        loginForm.setVisible(true);
    }

    private static void showSystemTrayIcon() {
        final TrayIcon trayIcon;

        if (SystemTray.isSupported()) {

            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("c:\\tray.png");

            MouseListener mouseListener = new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse clicked!");
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse entered!");
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse exited!");
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse pressed!");
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    System.out.println("Tray Icon - Mouse released!");
                }
            };

            ActionListener exitListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
            };

            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Exit");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);

            trayIcon = new TrayIcon(image, "Medici", popup);

            ActionListener actionListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    trayIcon.displayMessage("Action Event",
                            "An Action Event Has Been Performed!",
                            TrayIcon.MessageType.INFO);
                }
            };

            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
            trayIcon.addMouseListener(mouseListener);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

        } else {
            //  System Tray is not supported
        }
    }

    public static void main(final String[] args) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                dragDropWindow = new DragAndDropMedici();

                dragDropWindow.createAndShowGUI(args);
            }
        });
    }
}
