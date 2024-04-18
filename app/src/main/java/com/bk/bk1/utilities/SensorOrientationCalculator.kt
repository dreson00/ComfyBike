package com.bk.bk1.utilities

import androidx.lifecycle.MutableLiveData
import com.bk.bk1.models.ExperimentalData
import com.bk.bk1.models.Imu
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class SensorOrientationCalculator {

    companion object {
        val fusedOrientationLiveData = MutableLiveData<ExperimentalData>()
    }

    // angular speeds from gyro
    private val gyro = FloatArray(3)

    // rotation matrix from gyro data
    private var gyroMatrix = FloatArray(9)

    // orientation angles from gyro matrix
    private val gyroOrientation = FloatArray(3)

    // magnetic field vector
    private val magnet = FloatArray(3)

    // accelerometer vector
    private val accel = FloatArray(3)

    // orientation angles from accel and magnet
    private val accMagOrientation = FloatArray(3)

    // final orientation angles from sensor fusion
    private val fusedOrientation = FloatArray(3)

    // accelerometer and magnetometer based rotation matrix
    private val rotationMatrix = FloatArray(9)

    val EPSILON = 0.001f
    private val MS2S = 1.0f / 1000.0f
    private var timestamp = 0f
    private var initState = true

    val FILTER_COEFFICIENT = 0.5f

//    private val bus = BusProvider.getEventBus()

    init {
        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;
    }

    fun onSensorChanged(imuData: Imu) {
        magnet[0] = (imuData.Body.arrayMagn.sumOf { it.x } / imuData.Body.arrayMagn.count()).toFloat()
        magnet[1] = (imuData.Body.arrayMagn.sumOf { it.y } / imuData.Body.arrayMagn.count()).toFloat()
        magnet[2] = (imuData.Body.arrayMagn.sumOf { it.z } / imuData.Body.arrayMagn.count()).toFloat()

        // copy new accelerometer data into accel array and calculate orientation
        accel[0] = (imuData.Body.arrayAcc.sumOf { it.x } / imuData.Body.arrayAcc.count()).toFloat()
        accel[1] = (imuData.Body.arrayAcc.sumOf { it.y } / imuData.Body.arrayAcc.count()).toFloat()
        accel[2] = (imuData.Body.arrayAcc.sumOf { it.z } / imuData.Body.arrayAcc.count()).toFloat()
        calculateAccMagOrientation()

        gyroFunction(imuData)

        calculateFusedOrientation()
    }

    fun calculateFusedOrientation() {
        val oneMinusCoeff = 1.0f - FILTER_COEFFICIENT

        /*
             * Fix for 179� <--> -179� transition problem:
             * Check whether one of the two orientation angles (gyro or accMag) is negative while the other one is positive.
             * If so, add 360� (2 * math.PI) to the negative value, perform the sensor fusion, and remove the 360� from the result
             * if it is greater than 180�. This stabilizes the output in positive-to-negative-transition cases.
             */

        // azimuth

        /*
             * Fix for 179� <--> -179� transition problem:
             * Check whether one of the two orientation angles (gyro or accMag) is negative while the other one is positive.
             * If so, add 360� (2 * math.PI) to the negative value, perform the sensor fusion, and remove the 360� from the result
             * if it is greater than 180�. This stabilizes the output in positive-to-negative-transition cases.
             */

        // azimuth
        if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
            fusedOrientation[0] =
                (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]).toFloat()
            fusedOrientation[0] -= if (fusedOrientation[0] > Math.PI) (2.0 * Math.PI).toFloat() else 0f
        } else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
            fusedOrientation[0] =
                (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI)).toFloat()
            fusedOrientation[0] -= if (fusedOrientation[0] > Math.PI) (2.0 * Math.PI).toFloat() else 0f
        } else {
            fusedOrientation[0] =
                FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0]
        }

        // pitch

        // pitch
        if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
            fusedOrientation[1] =
                (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]).toFloat()
            fusedOrientation[1] -= if (fusedOrientation[1] > Math.PI) (2.0 * Math.PI).toFloat() else 0f
        } else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
            fusedOrientation[1] =
                (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI)).toFloat()
            fusedOrientation[1] -= if (fusedOrientation[1] > Math.PI) (2.0 * Math.PI).toFloat() else 0f
        } else {
            fusedOrientation[1] =
                FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1]
        }

        // roll

        // roll
        if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
            fusedOrientation[2] =
                (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]).toFloat()
            fusedOrientation[2] -= if (fusedOrientation[2] > Math.PI) (2.0 * Math.PI).toFloat() else 0f
        } else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
            fusedOrientation[2] =
                (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI)).toFloat()
            fusedOrientation[2] -= if (fusedOrientation[2] > Math.PI) (2.0 * Math.PI).toFloat() else 0f
        } else {
            fusedOrientation[2] =
                FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2]
        }

        // overwrite gyro matrix and orientation with fused orientation
        // to comensate gyro drift

        // overwrite gyro matrix and orientation with fused orientation
        // to comensate gyro drift
        gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation)
        System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3)
//        produceExperimentalDataUpdatedEvent()
        fusedOrientationLiveData.postValue(
            ExperimentalData(
                fusedOrientation[1] * 180 / Math.PI,
                fusedOrientation[2] * 180 / Math.PI,
                fusedOrientation[0] * 180 / Math.PI
            )
        )
    }

    // calculates orientation angles from accelerometer and magnetometer output
    fun calculateAccMagOrientation() {
        if (android.hardware.SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            android.hardware.SensorManager.getOrientation(rotationMatrix, accMagOrientation)
        }
    }

    // This function is borrowed from the Android reference
    // at http://developer.android.com/reference/android/hardware/SensorEvent.html#values
    // It calculates a rotation vector from the gyroscope angular speed values.
    private fun getRotationVectorFromGyro(
        gyroValues: FloatArray,
        deltaRotationVector: FloatArray,
        timeFactor: Float
    ) {
        val normValues = FloatArray(3)

        // Calculate the angular speed of the sample
        val omegaMagnitude =
            sqrt((gyroValues[0] * gyroValues[0] + gyroValues[1] * gyroValues[1] + gyroValues[2] * gyroValues[2]).toDouble()).toFloat()

        // Normalize the rotation vector if it's big enough to get the axis
        if (omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude
            normValues[1] = gyroValues[1] / omegaMagnitude
            normValues[2] = gyroValues[2] / omegaMagnitude
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        val thetaOverTwo = omegaMagnitude * timeFactor
        val sinThetaOverTwo = sin(thetaOverTwo.toDouble()).toFloat()
        val cosThetaOverTwo = cos(thetaOverTwo.toDouble()).toFloat()
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0]
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1]
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2]
        deltaRotationVector[3] = cosThetaOverTwo
    }

    // This function performs the integration of the gyroscope data.
    // It writes the gyroscope based orientation into gyroOrientation.
    fun gyroFunction(imuData: Imu) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null) return

        // initialisation of the gyroscope based rotation matrix
        if (initState) {
            var initMatrix = FloatArray(9)
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation)
            val test = FloatArray(3)
            android.hardware.SensorManager.getOrientation(initMatrix, test)
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix)
            initState = false
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        val deltaVector = FloatArray(4)
        if (timestamp != 0f) {
            val dT = (imuData.Body.Timestamp - timestamp) * MS2S
            gyro[0] = (imuData.Body.arrayGyro.sumOf { it.x } / imuData.Body.arrayGyro.count()).toFloat()
            gyro[1] = (imuData.Body.arrayGyro.sumOf { it.y } / imuData.Body.arrayGyro.count()).toFloat()
            gyro[2] = (imuData.Body.arrayGyro.sumOf { it.z } / imuData.Body.arrayGyro.count()).toFloat()
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f)
        }

        // measurement done, save current time for next interval
        timestamp = imuData.Body.Timestamp.toFloat()

        // convert rotation vector into rotation matrix
        val deltaMatrix = FloatArray(9)
        android.hardware.SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector)

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix)

        // get the gyroscope based orientation from the rotation matrix
        android.hardware.SensorManager.getOrientation(gyroMatrix, gyroOrientation)
    }

    private fun getRotationMatrixFromOrientation(o: FloatArray): FloatArray {
        val xM = FloatArray(9)
        val yM = FloatArray(9)
        val zM = FloatArray(9)
        val sinX = sin(o[1].toDouble()).toFloat()
        val cosX = cos(o[1].toDouble()).toFloat()
        val sinY = sin(o[2].toDouble()).toFloat()
        val cosY = cos(o[2].toDouble()).toFloat()
        val sinZ = sin(o[0].toDouble()).toFloat()
        val cosZ = cos(o[0].toDouble()).toFloat()

        // rotation about x-axis (pitch)
        xM[0] = 1.0f
        xM[1] = 0.0f
        xM[2] = 0.0f
        xM[3] = 0.0f
        xM[4] = cosX
        xM[5] = sinX
        xM[6] = 0.0f
        xM[7] = -sinX
        xM[8] = cosX

        // rotation about y-axis (roll)
        yM[0] = cosY
        yM[1] = 0.0f
        yM[2] = sinY
        yM[3] = 0.0f
        yM[4] = 1.0f
        yM[5] = 0.0f
        yM[6] = -sinY
        yM[7] = 0.0f
        yM[8] = cosY

        // rotation about z-axis (azimuth)
        zM[0] = cosZ
        zM[1] = sinZ
        zM[2] = 0.0f
        zM[3] = -sinZ
        zM[4] = cosZ
        zM[5] = 0.0f
        zM[6] = 0.0f
        zM[7] = 0.0f
        zM[8] = 1.0f

        // rotation order is y, x, z (roll, pitch, azimuth)
        var resultMatrix = matrixMultiplication(xM, yM)
        resultMatrix = matrixMultiplication(zM, resultMatrix!!)
        return resultMatrix
    }

    private fun matrixMultiplication(A: FloatArray, B: FloatArray): FloatArray {
        val result = FloatArray(9)
        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6]
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7]
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8]
        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6]
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7]
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8]
        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6]
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7]
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8]
        return result
    }

//    @Produce
//    fun produceExperimentalDataUpdatedEvent(): ExperimentalDataUpdatedEvent {
//        return ExperimentalDataUpdatedEvent(ExperimentalData(fusedOrientation[0].toDouble(), fusedOrientation[1].toDouble()))
//    }


}