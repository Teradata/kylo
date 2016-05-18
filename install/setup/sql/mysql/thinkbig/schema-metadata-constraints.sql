alter table CHANGE_SET add constraint FK5oo4mqjuwjfgb1581g6kxiqx9 foreign key (dataset_id) references DATASET (id);
alter table CHANGE_SET_FILES add constraint FK6phe0sauir4we82lyc2u2aai7 foreign key (id) references CHANGE_SET (id);
alter table CHANGE_SET_FILES_PATH add constraint FK1t44p83myhihorkybm8t4ljsx foreign key (change_set_files_id) references CHANGE_SET_FILES (id);
alter table CHANGE_SET_HIVE_TABLE add constraint FK8q6ytindtoluw2xfwwy218a3b foreign key (id) references CHANGE_SET (id);
alter table CHANGE_SET_HIVE_TABLE_PART_VALUE add constraint FKop7wjrlfh06h8544k9uahf35k foreign key (change_set_hive_table_id) references CHANGE_SET_HIVE_TABLE (id);
alter table DATA_OPERATION add constraint FKcxlae96uggnhy2sh7najjxu7d foreign key (dataset_id) references DATASET (id);
alter table DATA_OPERATION add constraint FKd6xpyscn8jfghycm3b3sv1wq2 foreign key (producer_id) references FEED_DESTINATION (id);
alter table DATASET add constraint FKfcan42ycg3hs53y2wuevue6bl foreign key (datasource_id) references DATASOURCE (id);
alter table DATASOURCE add constraint UK_jij3o7a4n3wrawxo45vmoobqf unique (name);
alter table FEED add constraint UK_j4px0sd8c2k3ycpw6uvqplrr unique (display_name);
alter table FEED add constraint UK_m3uusi4w4t57ey2153r1pwu32 unique (name);
alter table FEED add constraint FKi6tlfq6nytlrb8429acoo6f3s foreign key (sla_id) references SLA (id);
alter table FEED_DESTINATION add constraint FKm91lqsf5q7b3jltbb3h23upqy foreign key (datasource_id) references DATASOURCE (id);
alter table FEED_DESTINATION add constraint FKq7fugjfa8oliwli38vf8amdr2 foreign key (feed_id) references FEED (id);
alter table FEED_PROPERTIES add constraint FK67wcouxn6tab9gxv2u00gcn43 foreign key (JpaFeed_id) references FEED (id);
alter table FEED_SOURCE add constraint FKmf5yjhbgipi4ufuxj0wmme6gx foreign key (datasource_id) references DATASOURCE (id);
alter table FEED_SOURCE add constraint FK7cu1dl0vf0haaasfvshdsf4g7 foreign key (feed_id) references FEED (id);
alter table FEED_SOURCE add constraint FK5c9kcq5r4qunp058v6smsprb1 foreign key (agreement_id) references SLA (id);
alter table SLA add constraint UK_9hpi9lvo1tco0r1cuos6i4c0x unique (name);
alter table SLA_METRIC add constraint FK6nabgra4jm6p63me7qkqg8pev foreign key (obligation_id) references SLA_OBLIGATION (id);
alter table SLA_OBLIGATION add constraint FKn1t5kx4s83yu0sth86xawrixh foreign key (group_id) references SLA_OBLIGATION_GROUP (id);
alter table SLA_OBLIGATION_GROUP add constraint FKowk48beqvytrqxdxoejj1mjrb foreign key (agreement_id) references SLA (id);

/* FEED MANAGER TABLE FK Constraints */

ALTER TABLE `FM_FEED`
ADD CONSTRAINT `FM_TEMPLATE_ID_FK`
  FOREIGN KEY (`template_id`)
  REFERENCES `FM_TEMPLATE` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `FEED`
ADD CONSTRAINT `FM_CATEGORY_ID_FK`
  FOREIGN KEY (`category_id`)
  REFERENCES `FM_CATEGORY` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;


ALTER TABLE `CATEGORY`
ADD UNIQUE INDEX `category_name_uq` (`name` ASC),
ADD UNIQUE INDEX `display_name_uq` (`display_name` ASC);
