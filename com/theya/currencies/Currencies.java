package com.theya.currencies;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Currencies extends JFrame implements ActionListener{

    JButton walutyButton = new JButton("Sciagnij Waluty");
    JButton przeliczButton = new JButton("Przelicz PLN");
    JSpinner spinner;
    SpinnerDateModel dataModel;
    JTable walutyTable;
    DefaultTableModel tableModel = new DefaultTableModel(new Object[] { "Nazwa", "Przelicznik", "Kod", "Kurs" }, 0);
    JLabel infoLabel;

    ArrayList<String> recordsDates = new ArrayList<String>();
    ArrayList<Object[]> recordsXML = new ArrayList<Object[]>();

    public Currencies() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JLabel dateLabel = new JLabel("Data: ");
        Date today = new Date();
        dataModel = new SpinnerDateModel(today, null, null, Calendar.DAY_OF_YEAR);
        spinner = new JSpinner(dataModel);
        JSpinner.DateEditor dateSpinner = new JSpinner.DateEditor(spinner, "dd/MM/yy");
        spinner.setEditor(dateSpinner);
        przeliczButton.setEnabled(false);

        topPanel.add(dateLabel);
        topPanel.add(spinner);
        topPanel.add(walutyButton);
        topPanel.add(przeliczButton);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setVisible(true);
        walutyTable = new JTable(tableModel);
        JScrollPane pane = new JScrollPane(walutyTable);
        pane.getVerticalScrollBar().setUnitIncrement(1);
        listPanel.add(pane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        infoLabel = new JLabel(" ");
        bottomPanel.add(infoLabel);

        add(topPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        walutyButton.addActionListener(this);
        przeliczButton.addActionListener(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Waluty");
        setSize(500, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public String searchDir(Date date) {
        String temp = null;

        if(recordsDates.isEmpty()) {            //<< jezeli wpisy z pliku dir.txt nie istnieja, nalezy je sciagnac
            try {
                InputStream txtFile = new URL("http://www.nbp.pl/kursy/xml/dir.txt").openStream();
                InputStreamReader reader = new InputStreamReader(txtFile);
                BufferedReader txtInput = new BufferedReader(reader);
                String line;
                while((line = txtInput.readLine()) != null) {
                    if(line.charAt(0) == 'a') {
                        recordsDates.add(line);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        boolean isRecordFound = false;              //<< sprawdzenie czy rekord znaleziony
        boolean notifyFlag = false;                 //<< flaga sprawdzajaca, czy user powinien dostac zawiadomienie o nie istniejacym wpisie
        while(!isRecordFound) {
            for (String recordsDate : recordsDates) {
                if (recordsDate.substring(5).equals(convertDate(calendar))) {           //<< znaleziono podana date
                    isRecordFound = true;
                    temp = recordsDate;
                }
            }
            if(!isRecordFound) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);            //<<nie znaleziono podanej daty, algorytm bedzie szukac daty z poprzedniego dnia
                notifyFlag = true;
            }
            if(calendar.get(Calendar.YEAR) < 2002) {
                isRecordFound = true;
                temp = recordsDates.get(0);
                notifyFlag = true;
            }
        }
        if(notifyFlag) {
            JOptionPane.showMessageDialog(null, "Nie znaleziono wpisu z danego dnia. Scianiety zostanie wpis z poprzedzajacej daty.", "Uwaga!", JOptionPane.WARNING_MESSAGE);
        }

        return temp;
    }

    public String convertDate(Calendar cal) {
        return new SimpleDateFormat("yyMMdd").format(cal.getTime());
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if(source == walutyButton) {
            try {
                String temporary = searchDir(dataModel.getDate());          //<< przeszukaj dir.txt i znajdz date, albo pierwsza poprzedzajaca
                if(temporary != null) {
                    InputStream plikXML = new URL("http://www.nbp.pl/kursy/xml/" + temporary + ".xml").openStream();
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document dokument = builder.parse(plikXML);         //<< dokument xml zaladowany

                    dokument.getDocumentElement().normalize();          //<< normalizacja dokumentu

                    infoLabel.setText("Tabela: " + dokument.getElementsByTagName("numer_tabeli").item(0).getTextContent() + "          |          Opublikowana: " + dokument.getElementsByTagName("data_publikacji").item(0).getTextContent());

                    NodeList lista = dokument.getElementsByTagName("pozycja");          //<< sciagnij wszystkie "pozycje" z pliku

                    tableModel.setRowCount(0);              //<< wyzeruj tablice wpisow

                    for(int i=0; i<lista.getLength(); i++) {
                        Node rekord = lista.item(i);

                        if(rekord.getNodeType() == Node.ELEMENT_NODE) {               //<< jezeli wpis jest elementem, sciagnij z niego wszystkie dane i dodaj do tablicy
                            Element element = (Element) rekord;
                            Object[] temp = new Object[] {
                                    element.getElementsByTagName("nazwa_waluty").item(0).getTextContent(),
                                    element.getElementsByTagName("przelicznik").item(0).getTextContent(),
                                    element.getElementsByTagName("kod_waluty").item(0).getTextContent(),
                                    element.getElementsByTagName("kurs_sredni").item(0).getTextContent()
                            };
                            tableModel.addRow(temp);
                            recordsXML.add(temp);           //<< wpisy sa zapisywane do osobnej tablicy, w razy gdyby user chcial przeliczyc PLN
                            przeliczButton.setEnabled(true);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(source == przeliczButton) {
            new ConvertWindow(recordsXML);
        }
    }

    public static void main(String argv[]) {
        new Currencies();
    }
}
