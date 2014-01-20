/*
 * The MIT License (MIT)
 *
 * Copyright (c) 1/19/14 9:04 AM robberer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;


public class GUI extends JFrame {
    private JPanel panelResult;
    private JPanel panelLogfile;
    private JLabel status;
    private JLabel anzeige1;
    private JLabel anzeige2;
    private JTextArea textArea;

    private String addr;
    private int counter;
    private final static String newline = "\n";

    private boolean isStarted = false;

    private Worker worker = new Worker();

    private JButton startButton = new JButton(new AbstractAction("Start / Stop") {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!isStarted) {
                worker = new Worker();
                worker.execute();
                isStarted = true;
                status.setText("Refresh Thread: running");
            } else {
                worker.setStopFlag();
                worker.cancel(true);
                isStarted = false;
                status.setText("Refresh Thread: stopped");
            }
        }

    });

    public GUI() {
        super("Monitor");
        setLocation(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setResizable(false);

        //Das BorderLayout ist mal das erste - später fügen wir noch ein GridLayout im Westen hinzu
        getContentPane().setLayout(new BorderLayout());

        // Create the tab pages
		createPageResult();
		createPageLogfile();

        // Erzeugung eines JTabbedPane-Objektes
        JTabbedPane tabpane = new JTabbedPane(JTabbedPane.TOP);
        tabpane.addTab("Result", panelResult);
        tabpane.addTab("Logfile", panelLogfile);
        getContentPane().add(BorderLayout.CENTER, tabpane);

        pack();
        setVisible(true);
    }

    public void createPageResult() {
        panelResult = new JPanel();
        panelResult.setLayout( new BorderLayout() );

        JPanel panelResultLeft = new JPanel(new GridLayout(3, 1));
        JPanel panelResultRight = new JPanel(new GridLayout(3, 1));
        panelResultRight.setPreferredSize(new Dimension(100, 0));
        panelResultRight.setMinimumSize(new Dimension(100, 0));

        //Buttons erzeugen
        JButton button1 = new JButton("Nixbitch 1");
        button1.setFocusable(false);
        JButton button2 = new JButton("Nixbitch 2");
        button2.setFocusable(false);

        //Auf Panel Buttons packen
        panelResultLeft.add(button1);
        panelResultLeft.add(button2);
        panelResultLeft.add(startButton);

        //Listener für Buttons
        addButtonListener(button1);
        addButtonListener(button2);

        // Anzeige Labels restellen
        anzeige1 = new JLabel("Click Button");
        anzeige1.setHorizontalAlignment(JLabel.LEFT);
        anzeige1.setOpaque(true);
        anzeige2 = new JLabel("Click Button");
        anzeige2.setHorizontalAlignment(JLabel.LEFT);
        anzeige2.setOpaque(true);

        //Labels erzeugen
        status = new JLabel("Uebersicht");
        status.setText("Refresh Thread: stopped");

        // Auf Panel Labels packen
        panelResultRight.add(anzeige1);
        panelResultRight.add(anzeige2);

        panelResult.add(panelResultLeft, BorderLayout.WEST);
        panelResult.add(panelResultRight, BorderLayout.EAST);
        panelResult.add(status, BorderLayout.SOUTH);


    }

    public void createPageLogfile()
    {
        panelLogfile = new JPanel();
        panelLogfile.setLayout( new BorderLayout() );

        // Textarea erzeugen
        textArea = new JTextArea(5, 18);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText("Logfile started");

        // Scrollpane fuer Textarea
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(textArea);

        panelLogfile.add(scrollPane, BorderLayout.CENTER);

        //panel2.add( new JButton( "North" ), BorderLayout.NORTH );
        //panel2.add( new JButton( "South" ), BorderLayout.SOUTH );
        //panel2.add( new JButton( "East" ), BorderLayout.EAST );
        //panel2.add( new JButton( "West" ), BorderLayout.WEST );
        //panel2.add( new JButton( "Center" ), BorderLayout.CENTER );
    }

    private void addButtonListener(JButton b) {
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (ae.getActionCommand().equals("Nixbitch 2")) {
                    addr = "192.168.10.214";
                    eingabe(addr);
                } else if (ae.getActionCommand().equals("Nixbitch 1")) {
                    addr = "192.168.77.64";
                    eingabe(addr);
                }
            }
        });
    }

    private void eingabe(String addr) {
        Double load = Double.parseDouble(snmpGet(addr, "snmp4me", ".1.3.6.1.4.1.2021.10.1.3.1"));
        int v = getLoadColorId(load);

        textArea.append("check: " + addr + "(" + counter + ")" + newline);

        if (addr.equals("192.168.77.64")) {
            if (v == 0) anzeige1.setBackground(Color.green);
            if (v == 1) anzeige1.setBackground(Color.yellow);
            if (v == 2) anzeige1.setBackground(Color.red);
            if (v == 255) anzeige1.setBackground(Color.red);
            anzeige1.setText(String.valueOf(load));
        }
        if (addr.equals("192.168.10.214")) {
            if (v == 0) anzeige2.setBackground(Color.green);
            if (v == 1) anzeige2.setBackground(Color.yellow);
            if (v == 2) anzeige2.setBackground(Color.red);
            if (v == 255) anzeige2.setBackground(Color.red);
            anzeige2.setText(String.valueOf(load));
        }

        // wie oft wurde abgefragt ?
        counter++;
    }

    public Integer getLoadColorId(Double load) {
        if (load < 0.33) {
            return 0;
        }
        if (load > 0.33 && load < 0.66) {
            return 1;
        }
        if (load > 0.66) {
            return 2;
        }

        return 255;
    }

    class Worker extends SwingWorker<Void, Integer> {

        int counter = 0;

        private boolean stopFlag = false;

        @Override
        protected Void doInBackground() throws Exception {
            while (true) {
                counter++;
                publish(counter);
                Thread.sleep(3000);
                if (stopFlag)
                    break;
            }
            return null;
        }

        @Override
        protected void process(List<Integer> chunk) {

            // get last result
            //Integer counterChunk = chunk.get(chunk.size() - 1);
            //anzeige1.setText(counterChunk.toString());
            eingabe("192.168.10.214");
            eingabe("192.168.77.64");
        }

        void setStopFlag() {
            stopFlag = true;
        }
    }


    public String snmpGet(String host, String community, String strOID) {
        String strResponse = "";
        ResponseEvent response;
        Snmp snmp;
        try {
            OctetString community1 = new OctetString(community);
            host = host + "/" + "161";
            Address tHost = new UdpAddress(host);
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();
            CommunityTarget comtarget = new CommunityTarget();
            comtarget.setCommunity(community1);
            comtarget.setVersion(SnmpConstants.version1);
            comtarget.setAddress(tHost);
            comtarget.setRetries(0);
            comtarget.setTimeout(2000);
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(strOID)));
            pdu.setType(PDU.GET);
            snmp = new Snmp(transport);
            response = snmp.get(pdu, comtarget);
            if (response != null) {
                if (response.getResponse().getErrorStatusText().equalsIgnoreCase("Success")) {
                    PDU pduresponse = response.getResponse();
                    strResponse = pduresponse.getVariableBindings().firstElement().toString();
                    if (strResponse.contains("=")) {
                        int len = strResponse.indexOf("=");
                        strResponse = strResponse.substring(len + 1, strResponse.length());
                    }
                }
            } else {
                //System.out.println("Looks like a TimeOut occured ");
                textArea.append("Looks like a TimeOut occured" + newline);
            }
            snmp.close();
        } catch (Exception e) {
            //e.printStackTrace();
            textArea.append("Looks like a Error occured" + newline);
        }
        //System.out.println("Response="+strResponse);
        return strResponse;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI();
            }
        });
    }

}