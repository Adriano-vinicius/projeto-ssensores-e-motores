# 🤖 Projeto VMX-Pi: Hello Robot com Desvio de Obstáculos

Este repositório contém o código de controle e a documentação técnica para o robô educacional baseado no controlador **VMX-Pi** e placa de expansão de motores **Titan**, programado em Java utilizando o ecossistema WPILib adaptado pela Studica.

---

## 📅 Bloco 1: Elétrica e Sensores (Dias 5 ao 9)

### 🔌 Checklist Elétrico Estrutural (Dia 5 & 9)
O sistema elétrico foi inspecionado de ponta a ponta para garantir estabilidade lógica na VMX e potência correta nos motores da Titan.

| Componente / Ponto Crítico | Parâmetro Esperado | Valor Medido / Status | Evidência Visual / Registro |
| :--- | :--- | :--- | :--- |
| **Bateria Principal** | > 12.0 VDC | **12.6 VDC** | *Registrado via Multímetro na bancada* |
| **Control Panel / VMX IO** | Linha de 5V estável para sensores | **OK (5.02V)** | *Leds indicadores acesos em verde estável* |
| **Barramento CAN Titan** | Comunicação com ID correto do projeto | **OK (ID Correspondente)** | *Logs do barramento sem pacotes perdidos* |
| **Fusíveis de Proteção** | Totalmente inseridos e sem rompimento | **OK** | *Inspeção visual realizada na placa Titan* |
| **Rotas de Cabeamento** | Cabos organizados, sem dobras agudas | **OK** | *Linhas de alimentação e sinal separadas* |

### 🛠️ Diagnóstico de Falha em Bancada (Dia 6)
* **Falha Identificada:** Instabilidade de leitura e retorno constante de `0.0 cm` no sensor ultrassônico.
* **Diagnóstico:** Ruído elétrico ou falha física nos pinos de pulso (`TRIGGER` ou `ECHO`). No código, implementamos um filtro para ignorar leituras de `0` (visto que `distancia > 0`).
* **Ação Corretiva:** Reaperto e substituição dos cabos jumpers fêmea-fêmea conectados às portas DIO da VMX. Após a troca, a continuidade do sinal foi restaurada.

### 📡 Tabela de Calibração de Sensores (Dia 7 & 9)
O robô processa as leituras e atua defensivamente com base nos limites definidos em software.

| Sensor | Porta / Canal | Valor Crítico (Corte) | Comportamento Prático Obtido | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Ultrassônico HC-SR04** | Portas Configuradas | `<= 55.0 cm` | **Parada e giro imediato ao ler 55.0 cm ou menos** | **Aprovado (Ver Vídeo)** |
| **Botão START Físico** | Entrada Digital | Borda de Descida | **Altera o estado para `ANDANDO` e ativa o MockDS** | **Aprovado** |
| **Botão STOP Físico** | Entrada Digital | Borda de Descida | **Corta motores imediatamente e muda para `FINALIZADO`**| **Aprovado** |

> 🎥 **Evidência do Ultrassônico:** O vídeo `video_ultrassonico_parede.mp4` valida o momento exato em que o robô caminha em direção à parede e, ao cruzar o limiar de **55.0 cm**, para os motores e inicia a rotina de giro protegendo a integridade mecânica.

---

## 🔩 Bloco 2: Estrutura Mecânica e Chassi (Dias 10 ao 14)

### 🔧 Organização de Bancada e Ajustes (Dia 10 & 11)
A montagem do chassi de 3 motores (Configuração de tração diferencial/direcional) utilizou ferramentas de precisão (Chaves hexagonais e alicate de bico).
* **Alinhamento:** Os eixos dos motores 1 e 2 foram geometricamente alinhados para anular desvios angulares enquanto o robô executa a função `andarFrente()`.

### 🔍 Relatório de Inspeção de Competição (Dia 12, 13 & 14)
* **Pontos de Robustez:** Verificação de folgas nos acoplamentos dos motores Titan.
* **Fixação dos Sensores:** O sensor ultrassônico foi rigidamente fixado na parte frontal do chassi utilizando brackets plásticos para evitar oscilações na leitura de distância provocadas pela trepidação dos motores.
* **Manutenção Rápida:** Posicionamento estratégico da placa VMX e da chave de liga/desliga para fácil acesso em caso de necessidade de reset rápido em ambiente de testes ou arena.

---

## 🚀 Bloco 3: Programação e Controle (Dias 15 ao 19)

### 💻 Arquitetura da Máquina de Estados (`Robot.java`)
O código utiliza um loop de tempo fixo (`TimedRobot`) controlado por uma máquina de estados explícita (`EstadoRobo`), garantindo previsibilidade e segurança em cada fase do ciclo:

### 🛡️ Mecanismos de Parada Segura (Dia 17 & 19)
1.  **E-Stop Físico e Lógico:** Pressionar o botão físico conectado à entrada `BTN_STOP` interrompe o fluxo imediatamente, desabilita o simulador (`ds.disable()`) e força a execução do método `stopMotors()`, zerando a potência de todos os canais.
2.  **Tratamento de Falha do Sensor:** Caso o sensor perca comunicação ou retorne `0.0 cm`, o código descarta a leitura, evitando frenagens fantasmas ou comportamento errático no meio da pista.
3.  **Segurança em Modo Desabilitado:** Os métodos `disabledInit()` e `disabledPeriodic()` forçam os motores a `0.0` continuamente para evitar que o robô ande sozinho por persistência de sinal.
