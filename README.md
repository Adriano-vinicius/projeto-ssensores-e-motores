# 🤖 Projeto Hello Robot & Drivebase - Documentação Oficial

Este repositório contém a documentação, histórico de engenharia, checklist elétrico/mecânico e o código-fonte do robô desenvolvido para o cumprimento do cronograma de capacitação WPILib Java.

---

## 📅 Bloco 1: Elétrica e Sensores (Dias 5 ao 9)

### 🔌 Checklist Elétrico (Dia 5 & 9)
O sistema elétrico foi inspecionado, medido e validado conforme os parâmetros de segurança lógica e física.

| Componente / Ponto Crítico | Parâmetro Esperado | Valor Medido / Status | Evidência Visual |
| :--- | :--- | :--- | :--- |
| **Alimentação Principal** | > 12.0 VDC | **12.6 VDC** | *Ver imagem `bateria_multimetro.png`* |
| **Fusíveis (PDP/PDH)** | Todos operacionais e travados | **OK** | *Ver imagem `painel_central.png`* |
| **Control Panel / VMX IO** | Leds de status verdes (5V estável) | **OK** | *Ver imagem `vmx_status.png`* |
| **Titan / Motor Controllers** | Barramento CAN sem mau contato | **OK** | *Ver imagem `conexao_can.png`* |
| **Rotas de Cabeamento** | Cabos tensionados/vulneráveis? | **Nenhum (Organizado)**| *Ver imagem `rotas_cabos.png`* |

### 🛠️ Diagnóstico e Indução de Falha (Dia 6)
*   **Falha Induzida:** Desconexão intermitente do barramento CAN no Motor Controller da tração esquerda.
*   **Sintoma:** O robô entrava em *No Code* / Perda de comunicação com os motores esquerdos no Driver Station, emitindo logs de *Timeout* da CAN.
*   **Causa Raiz:** Crimpagem folgada no conector DuPont/JST.
*   **Ação Corretiva:** Substituição do terminal do cabo, nova crimpagem e fixação com trava-fios plástico. Barramento testado com multímetro acusando resistividade correta de ~60 Ohms.

### 📡 Validação de Sensores (Dia 7 & 9)
Os sensores foram testados em bancada e via código. Abaixo está a tabela de calibração comparativa:

| Sensor | Condição Prática do Teste | Valor Esperado | Valor Obtido nos Logs | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Ultrassônico** | Aproximação de obstáculo (Parede) | Parada a 30cm | **30.2 cm** | **Aprovado (Ver Vídeo)** |
| **Sharp IR** | Objeto posicionado a 15cm | Variação de ~1.5V | **1.48 V** | **Aprovado** |
| **Cobra Line** | Leitura de fita isolante preta no chão | Binário `1` (Preto) | **1** | **Aprovado** |

> 🎥 **Evidência em Vídeo:** O arquivo `video_ultrassonico_parede.mp4` na pasta `/evidencias` demonstra o robô lendo o sensor ultrassônico em tempo real e executando a lógica de reação segura ao detectar a parede.

---

## 🔩 Bloco 2: Estrutura Mecânica e Chassi (Dias 10 ao 14)

### 🔧 Organização de Bancada e Ferramental (Dia 10 & 11)
Para a montagem e ajuste do chassi principal, foram mapeados e organizados os seguintes recursos:
*   **Ferramentas:** Chaves Allen (padrão imperial e métrico), Torquímetro, Alicate de pressão e Paquímetro.
*   **Componentes do Chassi:** Brackets de fixação, perfis de alumínio, Rodas (Omni/Placas de tração), Rollers, Rolamentos e parafusos de alta resistência.

### 🔍 Relatório de Inspeção de Competição (Dia 12, 13 & 14)
Simulação de inspeção técnica de pista para garantir robustez e segurança contra vibrações de alto impacto:

1.  **Parafusos e Fixações:** Verificado torque visual em todos os brackets do chassi. Adicionado *Loctite Médio (Azul)* nos pontos de vibração do motor.
2.  **Peças em Impressão 3D / Policarbonato:** O suporte do sensor ultrassônico em 3D apresentava microfissuras. Foi substituído por uma versão com maior densidade de preenchimento (Infill 40%) e fixação com arruelas largas para distribuir a carga.
3.  **Folgas e Alinhamento:** Alinhamento das rodas verificado com paquímetro para mitigar desvios na odometria do robô durante trajetórias retas.

---

## 🚀 Bloco 3: Programação e Controle - Drivebase (Dias 15 ao 19)

### 💻 Arquitetura do Software
O código foi estruturado utilizando o padrão **Command-Based** da WPILib:
*   `DriveSubsystem`: Gerencia os motores, encoders, cálculo de cinemática e aplicação de Deadband.
*   `DriveTeleopCommand`: Vincula os eixos do Joystick aos métodos de movimentação do subsistema.

### 🛡️ Segurança e Métricas de Controle (Dia 17 & 19)
*   **Deadband Implementada:** Configurada em **5%** (`0.05`). Comandos do Joystick abaixo desse limite são ignorados para evitar *drifts* causados pelo desgaste físico do controle.
*   **Parada Segura (E-Stop Lógico):** Interrupção imediata da alimentação dos motores caso o sensor ultrassônico detecte distância crítica (< 20cm) ou o botão do Driver Station seja acionado.

---
