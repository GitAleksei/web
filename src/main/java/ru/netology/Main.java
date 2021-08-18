package ru.netology;

import static ru.netology.Conf.*;

public class Main {
  public static void main(String[] args) {
    Server server = new Server(NUMBER_OF_THREADS, VALID_PATH);
    server.listen(PORT);
  }
}


