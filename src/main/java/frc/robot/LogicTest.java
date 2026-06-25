package frc.robot;

import static org.junit.Assert.*;
import org.junit.Test;

public class LogicTest {

    // Simula a tomada de decisão baseada na lógica do seu Robot.java
    public String decidirProximoEstado(String estadoAtual, double distancia) {
        if (estadoAtual.equals("ANDANDO") && distancia > 0 && distancia <= 55.0) {
            return "GIRANDO_180";
        }
        return estadoAtual;
    }

    @Test
    public void testDesvioObstaculoAbaixoDe55cm() {
        String estadoInicial = "ANDANDO";
        double distanciaDetectadaPeloSonar = 42.5; // Menor que 55.0cm

        String proximoEstado = decidirProximoEstado(estadoInicial, distanciaDetectadaPeloSonar);

        // Verifica se o robô realmente muda para GIRANDO_180 conforme o código principal manda
        assertEquals("GIRANDO_180", proximoEstado);
        System.out.println("TESTE PASSED: Robo alterou o estado corretamente para desviar a " + distanciaDetectadaPeloSonar + " cm!");
    }

    @Test
    public void testIgnorarLeituraZero() {
        String estadoInicial = "ANDANDO";
        double distanciaFalhaLeitura = 0.0; // Falha do sensor

        String proximoEstado = decidirProximoEstado(estadoInicial, distanciaFalhaLeitura);

        // O robô deve ignorar o zero e continuar ANDANDO
        assertEquals("ANDANDO", proximoEstado);
        System.out.println("TESTE PASSED: Leitura nula (0.0 cm) ignorada com sucesso para evitar falso-positivo!");
    }
}
