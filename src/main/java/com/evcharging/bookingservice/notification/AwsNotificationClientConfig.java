package com.evcharging.bookingservice.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * Credentials come from the EC2 instance profile (evrecharge-ec2-role), same as every other AWS
 * call this service makes -- no keys are configured here.
 */
@Configuration
public class AwsNotificationClientConfig {

    @Value("${app.notifications.aws-region}")
    private String awsRegion;

    @Bean
    public SesV2Client sesV2Client() {
        return SesV2Client.builder().region(Region.of(awsRegion)).build();
    }

    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder().region(Region.of(awsRegion)).build();
    }
}
