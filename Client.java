package ru.geekbrains.chat;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

  class ClientWindow extends JFrame {
      // адрес сервера
      private static final String SERVER_HOST = "localhost";
      // порт
      private static final int SERVER_PORT = 1234;
      // клиентский сокет
      private Socket clientSocket;
      // входящее сообщение
      private Scanner inMessage;
      // исходящее сообщение
      private PrintWriter outMessage;
      // следующие поля отвечают за элементы формы
      private JTextField jtfMessage;
      private JTextField jtfName;
      private JTextArea jtaTextAreaMessage;
      // имя клиента
      private String clientName = "";

      // получаем имя клиента
      public String getClientName() {
          return this.clientName;
      }

      // конструктор
      public ClientWindow() {
          try {
              // подключаемся к серверу
              clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
              inMessage = new Scanner(clientSocket.getInputStream());
              outMessage = new PrintWriter(clientSocket.getOutputStream());
          } catch (IOException e) {
              e.printStackTrace();
          }
          // Задаём настройки элементов на форме
          setBounds(300, 100, 400, 400);
          setTitle("Client");

          JFrame frame = new JFrame();
          frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

          frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(WindowEvent e) {
                  JFrame frame = (JFrame) e.getSource();

                  int result = JOptionPane.showConfirmDialog(
                          frame,
                          "Are you sure you want to exit the application?",
                          "Exit Application",
                          JOptionPane.YES_NO_OPTION);

                  if (result == JOptionPane.YES_OPTION)
                      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
              }
          });

//        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
          JPanel bottomPanel = new JPanel(new BorderLayout());

          jtaTextAreaMessage = new JTextArea();
          jtaTextAreaMessage.setEditable(false);
          jtaTextAreaMessage.setLineWrap(true);
          JScrollPane jsp = new JScrollPane(jtaTextAreaMessage);
          add(jsp, BorderLayout.CENTER);
          // label, который будет отражать количество клиентов в чате
          JLabel jlNumberOfClients = new JLabel("Количество клиентов в чате: ");
          add(jlNumberOfClients, BorderLayout.NORTH);
          add(bottomPanel, BorderLayout.SOUTH);
          JButton jbSendMessage = new JButton("Отправить");
          bottomPanel.add(jbSendMessage, BorderLayout.EAST);
          jtfMessage = new JTextField("Введите ваше сообщение: ");
          bottomPanel.add(jtfMessage, BorderLayout.CENTER);
          jtfName = new JTextField("Введите ваше имя: ");
          bottomPanel.add(jtfName, BorderLayout.WEST);
          // обработчик события нажатия кнопки отправки сообщения
          jbSendMessage.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                  // если имя клиента, и сообщение непустые, то отправляем сообщение
                  if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty()) {
                      clientName = jtfName.getText();
                      sendMsg();
                      // фокус на текстовое поле с сообщением
                      jtfMessage.grabFocus();
                  }
              }
          });
        // при фокусе поле сообщения очищается
        jtfMessage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfMessage.setText("");
            }
        });
        // при фокусе поле имя очищается
        jtfName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfName.setText("");
            }
        });
          // в отдельном потоке начинаем работу с сервером
          new Thread(new Runnable() {
              @Override
              public void run() {
                  try {
                      // бесконечный цикл
                      while (true) {
                          // если есть входящее сообщение
                          if (inMessage.hasNext()) {
                              // считываем его
                              String inMes = inMessage.nextLine();
                              String clientsInChat = "Клиентов в чате = ";
                              if (inMes.indexOf(clientsInChat) == 0) {
                                  jlNumberOfClients.setText(inMes);
                              } else {
                                  // выводим сообщение
                                  jtaTextAreaMessage.append(inMes);
                                  // добавляем строку перехода
                                  jtaTextAreaMessage.append("\n");
                              }
                          }
                      }
                  } catch (Exception e) {
                  }
              }
          }).start();
          // добавляем обработчик события закрытия окна клиентского приложения
          JButton btnExit = new JButton("Exit chat");
          // 17.1 создаем обработчик событий
          btnExit.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                  System.exit(0);
              }
          });
          //  добавляем кнопку в панель
          bottomPanel.add(btnExit,BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    // здесь проверяем, что имя клиента непустое и не равно значению по умолчанию
                    if (!clientName.isEmpty() && clientName != "Введите ваше имя: ") {
                        outMessage.println(clientName + " вышел из чата!");
                    } else {
                        outMessage.println("Участник вышел из чата, так и не представившись!");
                    }
                    // отправляем служебное сообщение, которое является признаком того, что клиент вышел из чата
                    outMessage.println("##session##end##");
                    outMessage.flush();
                    outMessage.close();
                    inMessage.close();
                    clientSocket.close();
                } catch (IOException exc) {

                }
            }
      });

          // отображаем форму
          setVisible(true);
      }

    // отправка сообщения
    public void sendMsg() {
        // формируем сообщение для отправки на сервер
        String messageStr = jtfName.getText() + ": " + jtfMessage.getText();
        // отправляем сообщение
        outMessage.println(messageStr);
        outMessage.flush();
        jtfMessage.setText("");
    }

  }



