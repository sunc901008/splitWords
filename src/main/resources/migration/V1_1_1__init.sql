CREATE TABLE IF NOT EXISTS `history` (
  id               INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  question         TEXT                NOT NULL COMMENT 'search question',
  sourceList       TEXT                NOT NULL COMMENT 'source id list of the question(JSONArray string)',
  language         VARCHAR(45)         NOT NULL COMMENT 'current language',
  score            INT(11)             NOT NULL DEFAULT '0' COMMENT 'current question priority',
  userId           INT(11)             NOT NULL COMMENT 'user info id',
  creationDate     DATETIME            NOT NULL COMMENT 'create time',
  updationDate     DATETIME            NOT NULL COMMENT 'update time'
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
