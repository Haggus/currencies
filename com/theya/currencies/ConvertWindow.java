package com.theya.currencies;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

public class ConvertWindow extends JFrame implements ActionListener{

    ArrayList<Object[]> wpisy;
    JTextField plnField;
    JComboBox plnCombo;
    JButton plnConvertButton = new JButton("Przelicz");
    JLabel plnConvertedLabel;

    public ConvertWindow(ArrayList<Object[]> waluty) {
        wpisy = waluty;
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JLabel plnLabel = new JLabel("PLN: ");
        plnField = new JTextField(10);
        Vector comboBoxItems = new Vector();
        for (Object[] aWpisy : wpisy) {
            comboBoxItems.add(aWpisy[0]);
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(comboBoxItems);
        plnCombo = new JComboBox(model);
        topPanel.add(plnLabel);
        topPanel.add(plnField);
        topPanel.add(plnCombo);
        topPanel.add(plnConvertButton);

        JPanel bottomPanel = new JPanel();
        plnConvertedLabel = new JLabel(" ");
        plnConvertedLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        bottomPanel.add(plnConvertedLabel);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);

        plnConvertButton.addActionListener(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Przelicz PLN (wedlug kursu z wybranego dnia)");
        setSize(500, 120);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if(source == plnConvertButton) {
            if(!plnField.getText().equals("")) {
                if(isNumber(plnField.getText())) {
                    int index = plnCombo.getSelectedIndex();            //<< zapisz ktora waluta zostala wybrana
                    NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);              //<< waluty w tabeli uzywaja przecinka zamiast kropki, trzeba ta wartos przekonwerotwac
                    Number number = 0;
                    try {
                        number = format.parse(wpisy.get(index)[3].toString());
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    double kurs = number.doubleValue();
                    double calc = Double.parseDouble(plnField.getText()) / kurs;                //<< obliczanie wyniku
                    BigDecimal bd = new BigDecimal(calc);
                    bd = bd.setScale(2, RoundingMode.HALF_UP);                                  //<< wynik ma za duzo miejsc po przecinku, jest zaokraglany do 2 miejsc po przecinku (zaokraglony w gore)
                    plnConvertedLabel.setText(bd + " " + wpisy.get(index)[2].toString());
                } else {
                    JOptionPane.showMessageDialog(null, "Podana wartosc nie jest numerem.", "Uwaga!", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Prosze podac ilosc PLN.", "Uwaga!", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
