--
-- mysqldump -u root -t --compact --hex-blob --skip-extended-insert --complete-insert metadata
--
INSERT INTO `DATASOURCE` (`type`, `id`, `created_time`, `description`, `name`, `database_name`, `table_name`, `path`) VALUES ('directory',0x85AC5EFC0E7711E6BE09F1C2F9960556,'2016-04-29 19:01:58','Dummy dir datasource','dir-ds1',NULL,NULL,'file:/tmp/dir');
INSERT INTO `DATASOURCE` (`type`, `id`, `created_time`, `description`, `name`, `database_name`, `table_name`, `path`) VALUES ('hivetable',0xA5EBAB3C0E7711E6BE09F1C2F9960556,'2016-04-29 19:02:52','Dummy dir datasource','table-ds1','dummydb','dummytable1',NULL);
INSERT INTO `DATASOURCE` (`type`, `id`, `created_time`, `description`, `name`, `database_name`, `table_name`, `path`) VALUES ('hivetable',0xE763E524103511E6BE09F1C2F9960556,'2016-05-02 00:17:18','Table 2','table-ds2','dummydb','dummytable2',NULL);
INSERT INTO `DATA_OPERATION` (`id`, `start_time`, `state`, `status`, `stop_time`, `dataset_id`, `producer_id`) VALUES (0xF93976E6100911E6BE09F1C2F9960556,'2016-05-01 19:02:50','SUCCESS','','2016-05-01 19:02:50',NULL,NULL);
INSERT INTO `FEED` (`id`, `description`, `display_name`, `name`, `state`, `sla_id`) VALUES (0x1A0C60C6100611E6BE09F1C2F9960556,'',NULL,'a','ENABLED',NULL);
INSERT INTO `FEED` (`id`, `description`, `display_name`, `name`, `state`, `sla_id`) VALUES (0x1EE6BF6A100611E6BE09F1C2F9960556,'',NULL,'x','ENABLED',NULL);
INSERT INTO `FEED_DESTINATION` (`id`, `datasource_id`, `feed_id`) VALUES (0x0E34E382103711E6BE09F1C2F9960556,0xE763E524103511E6BE09F1C2F9960556,0x1EE6BF6A100611E6BE09F1C2F9960556);
INSERT INTO `FEED_DESTINATION` (`id`, `datasource_id`, `feed_id`) VALUES (0xED3C2CA6100611E6BE09F1C2F9960556,0xA5EBAB3C0E7711E6BE09F1C2F9960556,0x1A0C60C6100611E6BE09F1C2F9960556);
INSERT INTO `FEED_PROPERTIES` (`JpaFeed_id`, `prop_value`, `prop_key`) VALUES (0x1A0C60C6100611E6BE09F1C2F9960556,'val1','key1');
INSERT INTO `FEED_PROPERTIES` (`JpaFeed_id`, `prop_value`, `prop_key`) VALUES (0x1A0C60C6100611E6BE09F1C2F9960556,'val2','key2');
INSERT INTO `FEED_PROPERTIES` (`JpaFeed_id`, `prop_value`, `prop_key`) VALUES (0x1EE6BF6A100611E6BE09F1C2F9960556,'val3','key3');
INSERT INTO `FEED_SOURCE` (`id`, `datasource_id`, `feed_id`, `agreement_id`) VALUES (0x3AA5CAB6100611E6BE09F1C2F9960556,0x85AC5EFC0E7711E6BE09F1C2F9960556,0x1A0C60C6100611E6BE09F1C2F9960556,NULL);
INSERT INTO `FEED_SOURCE` (`id`, `datasource_id`, `feed_id`, `agreement_id`) VALUES (0xB8B7D162103611E6BE09F1C2F9960556,0xA5EBAB3C0E7711E6BE09F1C2F9960556,0x1EE6BF6A100611E6BE09F1C2F9960556,NULL);
