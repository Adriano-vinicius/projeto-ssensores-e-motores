package frc.robot.subsystems;

import com.studica.frc.Titan;
import edu.wpi.first.wpilibj.command.Subsystem;
import frc.robot.Constants;

public class DriveTrain extends Subsystem {

    private final Titan titan;

    private final Titan.Motor motor0; // M0
    private final Titan.Motor motor1; // M2
    private final Titan.Motor motor2; // M3

    public DriveTrain() {
        titan  = new Titan(Constants.TITAN_ID);

        motor0 = titan.getMotor(Constants.MOTOR_0); // porta 0
        motor1 = titan.getMotor(Constants.MOTOR_1); // porta 2
        motor2 = titan.getMotor(Constants.MOTOR_2); // porta 3
    }

    /**
     * Move os 3 motores com a mesma velocidade
     * @param velocidade -1.0 (ré) até 1.0 (frente)
     */
    public void mover(double velocidade) {
        motor0.set(velocidade);
        motor1.set(velocidade);
        motor2.set(velocidade);
    }

    /**
     * Para todos os motores
     */
    public void parar() {
        mover(0.0);
    }

    @Override
    protected void initDefaultCommand() {}
}