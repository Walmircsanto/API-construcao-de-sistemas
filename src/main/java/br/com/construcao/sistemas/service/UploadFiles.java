package br.com.construcao.sistemas.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Date;

@Service
public class UploadFiles {

    private final S3Client s3Client;

    private final String region;

    private final String bucketName;

    public UploadFiles(@Value("${aws.s3.bucket_name}") String bucketName,
                       @Value("${aws.region}") String region,
                       @Value("${aws.access.key}") String accessKey,
                       @Value("${aws.secret.key}") String secretKey) {
        this.bucketName = bucketName;
        this.region = region;

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(this.region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }


    public String  putObject(MultipartFile file ) throws IOException {

       //definir o nome do arquivo
        String fileName = file.getOriginalFilename() + "_" + new Date().getTime();
        String contentType = file.getContentType();

        PutObjectRequest putS3ObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

          try{
              s3Client.putObject(putS3ObjectRequest, RequestBody.fromInputStream(file.getInputStream(),file.getSize()));
          }catch (Exception e){
              System.out.println(e.getMessage());
          }

        return String.format("https://%s.s3.%s.amazonaws.com/%s", this.bucketName,region, fileName);

    }
}
