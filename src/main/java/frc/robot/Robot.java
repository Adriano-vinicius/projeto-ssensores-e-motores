package frc.robot;

import com.studica.frc.MockDS;
import com.studica.frc.Titan;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput; 
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.command.Scheduler;

public class Robot extends TimedRobot {

    private Titan titan;
    private Titan.Motor motor0;
    private Titan.Motor motor1;
    private Titan.Motor motor2;

    private MockDS ds;

    private DigitalInput btnStart;
    private DigitalInput btnStop;
    
    private DigitalOutput led1;
    private DigitalOutput led2;

    private boolean lastStart = false;
    private boolean lastStop = false;

    private boolean roboLiberado = false;

    private Ultrasonic sonar;
    private boolean sonarOk = false;

    private Timer printTimer = new Timer();
    private Timer giroTimer = new Timer();
    private Timer pausaTimer = new Timer(); // NOVO: Timer para controlar os 3 segundos parado

    private static final double DISTANCIA_PARAR_CM = 25.50;
    private static final double TEMPO_GIRO_180 = 2.10;
    private static final double TEMPO_PAUSA_SEGUNDOS = 3.00; // NOVO: Tempo de espera regulável

    private enum EstadoRobo {
        AGUARDANDO_START,
        ANDANDO,
        GIRANDO_180,
        PAUSADO, // NOVO: Estado intermediário de descanso antes de voltar a andar
        FINALIZADO
    }

    private EstadoRobo estado = EstadoRobo.AGUARDANDO_START;

    @Override
    public void robotInit() {
        titan = new Titan(Constants.TITAN_ID);

        motor0 = titan.getMotor(Constants.MOTOR_0);
        motor1 = titan.getMotor(Constants.MOTOR_1);
        motor2 = titan.getMotor(Constants.MOTOR_2);

        ds = new MockDS();

        btnStart = new DigitalInput(Constants.BTN_START);
        btnStop = new DigitalInput(Constants.BTN_STOP);
        
        led1 = new DigitalOutput(Constants.LED_1);
        led2 = new DigitalOutput(Constants.LED_2);

        try {
            sonar = new Ultrasonic(
                    Constants.ULTRASONIC_TRIGGER,
                    Constants.ULTRASONIC_ECHO
            );

            sonar.setAutomaticMode(true);
            sonarOk = true;
            System.out.println("Ultrassonico iniciado com sucesso.");
        } catch (Exception e) {
            sonarOk = false;
            System.out.println("Erro ao iniciar ultrassonico:");
            e.printStackTrace();
        }

        roboLiberado = false;
        estado = EstadoRobo.AGUARDANDO_START;

        printTimer.reset();
        printTimer.start();

        giroTimer.stop();
        giroTimer.reset();
        
        pausaTimer.stop();
        pausaTimer.reset();

        stopMotors();
        
        led1.set(true);
        led2.set(false);

        System.out.println("Robo iniciado.");
        System.out.println("Aguardando START fisico.");
    }

    @Override
    public void robotPeriodic() {
        Scheduler.getInstance().run();

        boolean curStart = btnStart.get();
        boolean curStop = btnStop.get();

        // START físico pressionado
        if (lastStart && !curStart) {
            roboLiberado = true;
            estado = EstadoRobo.ANDANDO;
            ds.enable();

            led1.set(false);
            led2.set(true);

            System.out.println("START pressionado: robo liberado.");
        }

        // STOP físico pressionado
        if (lastStop && !curStop) {
            roboLiberado = false;
            estado = EstadoRobo.FINALIZADO;
            ds.disable();
            stopMotors();

            led1.set(true);
            led2.set(false);

            System.out.println("STOP pressionado: robo parado definitivamente.");
        }

        lastStart = curStart;
        lastStop = curStop;

        if (printTimer.get() >= 0.5) {
            double distancia = getDistanceCm();
            System.out.printf("Estado: %s | Distancia lida: %.1f cm%n", estado, distancia);
            printTimer.reset();
        }
    }

    @Override
    public void autonomousInit() {
        System.out.println("Autonomo iniciado.");
    }

    @Override
    public void autonomousPeriodic() {
        executarLogica();
    }

    @Override
    public void teleopInit() {
        System.out.println("Teleop iniciado.");
    }

    @Override
    public void teleopPeriodic() {
        executarLogica();
    }

    private void executarLogica() {
        if (!roboLiberado) {
            stopMotors();
            return;
        }

        switch (estado) {
            case AGUARDANDO_START:
                stopMotors();
                break;

            case ANDANDO:
                andarFrente();
                double distancia = getDistanceCm();

                if (distancia > 0 && distancia <= DISTANCIA_PARAR_CM) {
                    stopMotors();
                    estado = EstadoRobo.GIRANDO_180;
                    giroTimer.reset();
                    giroTimer.start();
                    System.out.printf("Obstaculo detectado em %.1f cm. Iniciando giro 180.%n", distancia);
                }
                break;

            case GIRANDO_180:
                girar180();

                if (giroTimer.get() >= TEMPO_GIRO_180) {
                    giroTimer.stop();
                    giroTimer.reset();
                    stopMotors();

                    // MUDANÇA: Em vez de ir para FINALIZADO, vai para PAUSADO
                    estado = EstadoRobo.PAUSADO;
                    pausaTimer.reset();
                    pausaTimer.start();
                    
                    // Pisca os LEDs indicando que está aguardando temporariamente
                    led1.set(true);
                    led2.set(true);

                    System.out.println("Giro finalizado. Pausando por 3 segundos...");
                }
                break;

            case PAUSADO: // NOVO BLOCO: Controla a pausa de 3 segundos
                stopMotors(); // Garante que fica parado

                if (pausaTimer.get() >= TEMPO_PAUSA_SEGUNDOS) {
                    pausaTimer.stop();
                    pausaTimer.reset();
                    
                    // Configura os LEDs para modo de movimento novamente
                    led1.set(false);
                    led2.set(true);

                    // Reinicia o loop jogando o robô de volta para a pista!
                    estado = EstadoRobo.ANDANDO;
                    System.out.println("Pausa concluida! Voltando a andar para frente.");
                }
                break;

            case FINALIZADO:
                stopMotors();
                break;
        }
    }

    private double getDistanceCm() {
        if (!sonarOk || sonar == null) {
            return -1.0;
        }
        try {
            double distanciaMm = sonar.getRangeMM();
            return distanciaMm / 10.0;
        } catch (Exception e) {
            System.out.println("Erro lendo ultrassonico:");
            e.printStackTrace();
            return -1.0;
        }
    }

    private void andarFrente() {
        motor0.set(0.0);
        motor1.set(Constants.VELOCIDADE);
        motor2.set(-Constants.VELOCIDADE);
    }

    private void girar180() {
        motor0.set(Constants.VELOCIDADE);
        motor1.set(Constants.VELOCIDADE);
        motor2.set(Constants.VELOCIDADE);
    }

    private void stopMotors() {
        motor0.set(0.0);
        motor1.set(0.0);
        motor2.set(0.0);
    }

    @Override
    public void disabledPeriodic() {
        stopMotors();
        led1.set(true);
        led2.set(false);
    }

    @Override
    public void disabledInit() {
        stopMotors();
        led1.set(true);
        led2.set(false);
        System.out.println("Robo desabilitado: motores parados.");
    }
}