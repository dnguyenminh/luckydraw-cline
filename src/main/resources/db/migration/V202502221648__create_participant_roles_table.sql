CREATE TABLE participant_roles (
    participant_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    CONSTRAINT pk_participant_roles PRIMARY KEY (participant_id, role_id),
    CONSTRAINT fk_participant_roles_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX idx_participant_roles_participant_id ON participant_roles(participant_id);
CREATE INDEX idx_participant_roles_role_id ON participant_roles(role_id);

-- Assign default ROLE_PARTICIPANT to existing participants
INSERT INTO participant_roles (participant_id, role_id, created_by)
SELECT p.id, r.id, 'SYSTEM'
FROM participants p
CROSS JOIN roles r
WHERE r.name = 'ROLE_PARTICIPANT';