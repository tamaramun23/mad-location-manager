package com.example.gpsacckalmanfusion.Lib.Filters;

import com.example.gpsacckalmanfusion.Lib.Commons.Matrix;

/**
 * Created by lezh1k on 2/13/18.
 */

public class GPSAccKalmanFilter {
    private double m_timeStampMsPredict;
    private double m_timeStampMsUpdate;
    private KalmanFilter m_kf;
    private double m_accSigma;

    public GPSAccKalmanFilter(double x, double y,
                              double xVel, double yVel,
                              double accDev, double posDev,
                              double timeStampMs) {

        m_kf = new KalmanFilter(4, 4, 1);
        m_timeStampMsPredict = m_timeStampMsUpdate = timeStampMs;
        m_accSigma = accDev;
        m_kf.Xk_k.setData(x, y, xVel, yVel);

        m_kf.H.setIdentityDiag(); //state has 4d and measurement has 4d too. so here is identity
        m_kf.Pk_k.setIdentity();
        m_kf.Pk_k.scale(posDev);
    }

    private void rebuildF(double dtPredict) {
        double f[] = {
                1.0, 0.0, dtPredict, 0.0,
                0.0, 1.0, 0.0, dtPredict,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0
        };
        m_kf.F.setData(f);
    }

    private void rebuildU(double xAcc,
                          double yAcc) {
        m_kf.Uk.setData(xAcc, yAcc);
    }

    private void rebuildB(double dtPredict) {
        double dt2 = 0.5*dtPredict*dtPredict;
        double b[] = {
                dt2, 0.0,
                0.0, dt2,
                dtPredict, 0.0,
                0.0, dtPredict
        };
        m_kf.B.setData(b);
    }

    private void rebuildR(double posSigma, double velSigma) {
        //Warning!!! this matrix depends on measure dimension.
        double R[] = {
                posSigma, 0.0, 0.0, 0.0,
                0.0, posSigma, 0.0, 0.0,
                0.0, 0.0, velSigma, 0.0,
                0.0, 0.0, 0.0, velSigma
        };
        m_kf.R.setData(R);
    }

    private void rebuildQ(double dtUpdate,
                          double accSigma) {
        m_kf.Q.setIdentity();
        m_kf.Q.scale(accSigma * dtUpdate);
    }

    public void predict(double timeNowMs,
                        double xAcc,
                        double yAcc) {
        double dtPredict = (timeNowMs - m_timeStampMsPredict) / 1000.0;
        double dtUpdate = (timeNowMs - m_timeStampMsUpdate) / 1000.0;
        rebuildF(dtPredict);
        rebuildB(dtPredict);
        rebuildU(xAcc, yAcc);

        rebuildQ(dtUpdate, m_accSigma); //empirical method. WARNING!!!

        m_timeStampMsPredict = timeNowMs;
        m_kf.predict();
        Matrix.matrixCopy(m_kf.Xk_km1, m_kf.Xk_k);
    }

    public void update(double timeStamp,
                       double x,
                       double y,
                       double xVel,
                       double yVel,
                       double posDev,
                       double velErr) {
        m_timeStampMsUpdate = timeStamp;
        rebuildR(posDev, velErr);
        m_kf.Zk.setData(x, y, xVel, yVel);
        m_kf.update();
    }

    public double getCurrentX() {
        return m_kf.Xk_k.data[0][0];
    }

    public double getCurrentY() {
        return m_kf.Xk_k.data[1][0];
    }

    public double getCurrentXVel() {
        return m_kf.Xk_k.data[2][0];
    }

    public double getCurrentYVel() {
        return m_kf.Xk_k.data[3][0];
    }

}
