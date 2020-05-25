package fi.hsl.transitloghfpapi.servehfp;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;

import java.sql.Date;
import java.util.*;

@Service
class HfpBatchService {
    private final HfpBlobFilenameStrategy hfpBlobFilenameStrategy;
    private final HfpCSVMapper hfpCSVMapper;
    private AzureDownload azureDownload;
    private HfpRepository hfpRepository;

    @Autowired
    public HfpBatchService(@Value("blobConnectionString") String blobConnectionString, @Value("containerName") String containerName, HfpRepository hfpRepository) {
        this.azureDownload = new AzureDownload(blobConnectionString, containerName);
        this.hfpBlobFilenameStrategy = new HfpBlobFilenameStrategy();
        this.hfpCSVMapper = new HfpCSVMapper();
        this.hfpRepository = hfpRepository;
    }


    ResponseEntity<String> createHFPCollectionJob(Date startDate, Date endDate) {
        final List<String> listOfHfpFilenames = hfpBlobFilenameStrategy.createHfpFilenames(startDate, endDate);
        final List<AzureDownload.LocalBlob> localBlobs = azureDownload.downloadBlob(listOfHfpFilenames);

        localBlobs.parallelStream()
                .map(localBlob -> localBlob.createEntryList(hfpCSVMapper))
                .flatMap(Collection::stream)
                .forEach(event -> hfpRepository.save(event));

        return new ResponseEntity<>("Job accepted", HttpStatus.ACCEPTED);
    }
}
