package org.mytest.test.component.task;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author gemo
 * @date 2025/11/28 16:16
 */
public class McpReportTask {

    @Scheduled(cron = "0 * * * * ?")
    public void report() {

    }
}
