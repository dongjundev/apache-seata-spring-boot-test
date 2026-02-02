-- Seata undo_log table for MariaDB (AT Mode required)
CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'increment id',
    branch_id BIGINT NOT NULL COMMENT 'branch transaction id',
    xid VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    context VARCHAR(128) NOT NULL COMMENT 'undo_log context',
    rollback_info LONGBLOB NOT NULL COMMENT 'rollback info',
    log_status INT NOT NULL COMMENT '0:normal status,1:defense status',
    log_created TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'create datetime',
    log_modified TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'modify datetime',
    CONSTRAINT ux_undo_log UNIQUE (xid, branch_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

CREATE INDEX idx_undo_log_xid ON undo_log (xid);
CREATE INDEX idx_undo_log_created ON undo_log (log_created);
