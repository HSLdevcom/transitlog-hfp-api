package fi.hsl.transitloghfpapi.servehfp.api;

public class HfpJobNotFinishedException extends RuntimeException {
    public HfpJobNotFinishedException(String reason) {
        super(reason);
    }
}
