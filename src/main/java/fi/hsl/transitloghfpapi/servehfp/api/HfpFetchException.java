package fi.hsl.transitloghfpapi.servehfp.api;

public class HfpFetchException extends RuntimeException {
    public HfpFetchException(String reason) {
        super(reason);
    }
}
