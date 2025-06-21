/* Disciplina: Programacao Concorrente */
 /* Prof.: Silvana Rossetto */
 /* Laboratório: 11 */
 /* Codigo: Exemplo de uso de futures */
 /* -------------------------------------------------------------------*/

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//classe runnable
class MyCallable implements Callable<Long> {
    //construtor

    MyCallable() {
    }

    //método para execução
    public Long call() throws Exception {
        long s = 0;
        for (long i = 1; i <= 100; i++) {
            s++;
        }
        return s;
    }
}
class Primo implements Callable<Long> {
    long num;
    //construtor
    public Primo(long n) {
        num=n;
    }

    //método para execução
    public Long call() throws Exception {
        if (num == 0 || num == 1) {
            //System.out.printf("%d Não é primo\n", num);
            return (long) 0;
        }
        if (num > 2) {
            for (int i = 2; i < num; i++) {
                if (num % i == 0) {
                    //System.out.printf("%d Não é primo\n", num);
                    return (long) 0;
                }
            }
        }
        return (long) 1;
    }
}

//classe do método main
public class FutureHello {

    private static final int N = 100000;
    private static final int NTHREADS = 10;

    public static void main(String[] args) {
        //cria um pool de threads (NTHREADS)
        ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
        //cria uma lista para armazenar referencias de chamadas assincronas
        List<Future<Long>> list = new ArrayList<Future<Long>>();

        for (int i = 1; i <= N; i++) {
            //Callable<Long> worker = new MyCallable();
            Callable<Long> worker = new Primo(i);
            Future<Long> submit = executor.submit(worker);
            list.add(submit);
        }

        System.out.printf("Total de números primos até %d: ",list.size());
        //pode fazer outras tarefas...

        //recupera os resultados e faz o somatório final
        long sum = 0;
        for (Future<Long> future : list) {
            try {
                sum += future.get(); //bloqueia se a computação nao tiver terminado
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println(sum);
        executor.shutdown();
    }
}
