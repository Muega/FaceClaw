import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.objdetect.FaceDetectorYN;

public class FaceOcclusionDetector {
    static { System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME); }

    private final FaceDetectorYN detector;

    public FaceOcclusionDetector(String modelPath) {
        // scoreThresh niedriger für verdeckte Gesichter (0.35–0.45)
        this.detector = FaceDetectorYN.create(
            modelPath, "", new Size(320, 320), 0.40f, 0.50f, 5000
        );
    }

    public boolean hasFace(java.awt.image.BufferedImage bi) {
        Mat mat = toMat(bi);
        detector.setInputSize(new Size(mat.width(), mat.height()));
        Mat out = new Mat();
        detector.detect(mat, out);
        return out.rows() > 0;
    }

    private static Mat toMat(java.awt.image.BufferedImage bi) {
        java.awt.image.BufferedImage bgr = new java.awt.image.BufferedImage(
            bi.getWidth(), bi.getHeight(), java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
        bgr.getGraphics().drawImage(bi, 0, 0, null);
        byte[] data = ((java.awt.image.DataBufferByte)bgr.getRaster().getDataBuffer()).getData();
        Mat m = new Mat(bgr.getHeight(), bgr.getWidth(), CvType.CV_8UC3);
        m.put(0, 0, data);
        return m;
    }
}