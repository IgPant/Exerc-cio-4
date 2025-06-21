/* Disciplina: Programacao Concorrente */
 /* Prof.: Silvana Rossetto */
 /* Laboratório: 11 */
 /* Codigo: Criando um pool de threads em Java */

import java.util.LinkedList;

//-------------------------------------------------------------------------------
//Esta classe demonstra uma implementação "manual" de um pool de threads. Ela demonstra a criação de um número fixo de threads para um quantidade distinta de tarefas runnable. A classe então apresenta métodos para criação do pool, adição de tarefas na fila, interrupção da execução do pool e a classe de threads do pool em si, com seu método run.
class FilaTarefas {

    private final int nThreads;
    private final MyPoolThreads[] threads;
    private final LinkedList<Runnable> queue;
    private boolean shutdown;

    //Construtor da classe, recebendo o número desejado de threads como argumento. Inicia o "shutdown" como false e cria um array para threads do tipo MyPoolThreads do tamanho recebido. Em seguida, cria e inicializa uma thread por posição do array.
    public FilaTarefas(int nThreads) {
        this.shutdown = false;
        this.nThreads = nThreads;
        queue = new LinkedList<Runnable>();
        threads = new MyPoolThreads[nThreads];
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new MyPoolThreads();
            threads[i].start();
        }
    }

    //Este método adiciona tarefas na fila de tarefas. Ele é sincronizado, logo apenas uma intância do bloco de código pode ser executada de cada vez. Recebendo um objeto Runnable, ele adiciona esse objeto na fila "queue" se shutdown for falso. Ele notifica uma thread em wait, que pode prosseguir com a remoçao da tarefa e sua execução.
    public void execute(Runnable r) {
        synchronized (queue) {
            if (this.shutdown) {
                return;
            }
            queue.addLast(r); //Adiciona a tarefa no fim da fila
            queue.notify();
        }
    }

    //Esse método sinaliza o fim da fila de tarefas, notificando todas as threas em wait que, ao despertarem e encontrarem uma fila vazia, irão retornar, isto é, encerrarão sua atividade. Ele é sincronizado e utiliza o mesmo lock, queue, ou seja, apenas uma instância de bloco com lock queue pode rodar por vez, seja ela em execute, shutdown ou no método run da thread.Ela também faz um join() para cada thread, para que a thread chamadora, no caso a main, aguarde a finalização de cada thread do pool antes de prosseguir.
    public void shutdown() {
        synchronized (queue) {
            this.shutdown = true;
            queue.notifyAll();
        }
        for (int i = 0; i < nThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    //Esta é a classe aninha MyPoolThreads, que descreve o comportamento das threads no pool. Em loop, as threads, caso shutdown nao esteja ativo e fila vazia, obtém o lock queue e dormem, liberando o lock. Caso a fila receba tarefas, uma thread é despertada, remove a tarefa da fila referenciando-a por "r" e procede para a execução de seu método run. Caso seja despertada e a fila esteja vazia, sabe-se que foi por ação do shutdown, com notifyall(). Nesse caso, a thread encerra sua atividade com return;.
    private class MyPoolThreads extends Thread {

        public void run() {
            Runnable r;
            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty() && (!shutdown)) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                    if (queue.isEmpty()) {
                        return;
                    }
                    r = (Runnable) queue.removeFirst();
                }
                try {
                    r.run();
                } catch (RuntimeException e) {
                }
            }
        }
    }
}
//-------------------------------------------------------------------------------

//--PASSO 1: cria uma classe que implementa a interface Runnable 
class Hello implements Runnable {

    String msg;

    public Hello(String m) {
        msg = m;
    }

    //--metodo executado pela thread
    public void run() {
        System.out.println(msg);
    }
}

class Primo implements Runnable {

    int num;

    public Primo(int n) {
        num = n;
    }

    //...completar implementacao, recebe um numero inteiro positivo e imprime se esse numero eh primo ou nao
    public void run() {
        if (num == 0 || num == 1) {
            System.out.printf("%d Não é primo\n", num);
            return;
        }
        if (num > 2) {
            for (int i = 2; i < num; i++) {
                if (num % i == 0) {
                    System.out.printf("%d Não é primo\n", num);
                    return;
                }
            }
        }
        System.out.printf("%d é primo\n", num);
    }
}

//Classe da aplicação (método main)
class MyPool {

    private static final int NTHREADS = 10;

    public static void main(String[] args) {
        //--PASSO 2: cria o pool de threads
        FilaTarefas pool = new FilaTarefas(NTHREADS);

        //--PASSO 3: dispara a execução dos objetos runnable usando o pool de threads
        for (int i = 0; i < 250; i++) {
            //final String m = "Hello da tarefa " + i;
            //Runnable hello = new Hello(m);
            //pool.execute(hello);
            Runnable primo = new Primo(i);
            pool.execute(primo);
        }

        //--PASSO 4: esperar pelo termino das threads
        pool.shutdown();
        System.out.println("Terminou");
    }
}
