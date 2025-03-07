
    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_date timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_locations (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint,
        id bigserial not null,
        province_id bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table events (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        available_spins integer,
        daily_spin_count integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        participant_id bigint,
        updated_at timestamp(6),
        version bigint,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table provinces (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        region_id bigint,
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table regions (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255) unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        points_earned integer,
        points_spent integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_id bigint,
        reward_id bigint,
        timestamp timestamp(6),
        updated_at timestamp(6),
        version bigint,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_locked boolean,
        account_non_expired boolean,
        account_non_locked boolean,
        credentials_non_expired boolean,
        failed_attempts integer,
        password_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255),
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_locations 
       add constraint FKdquu070vft3scg2w79xy9secn 
       foreign key (event_id) 
       references events;

    alter table if exists event_locations 
       add constraint FKoputwo3ch1tbxhv4mp5m0p2gc 
       foreign key (province_id) 
       references provinces;

    alter table if exists event_locations 
       add constraint FKs41txxkfquf582772ahkwlub2 
       foreign key (region_id) 
       references regions;

    alter table if exists golden_hours 
       add constraint FKiextogf8tkm1q3gu9vblyswnq 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKd015ah4sdlr6ry5rx5nvnu6qa 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKapubofdx7bicmjxnngjaf4mcj 
       foreign key (province_id) 
       references provinces;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists provinces 
       add constraint FKr52p9hvmia0r4042b4s4h6qil 
       foreign key (region_id) 
       references regions;

    alter table if exists rewards 
       add constraint FK465nimj9nfk08cul0s94bu79n 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKn5ghsrn4a8u3tve4lpbat9cf1 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKqyc5uaa3ar8vpbrbom3tspk2x 
       foreign key (participant_id) 
       references participants;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    create table blacklisted_tokens (
        status integer not null,
        created_at timestamp(6),
        expiry_date timestamp(6) not null,
        id bigserial not null,
        updated_at timestamp(6),
        user_id bigint,
        version bigint,
        created_by varchar(255),
        metadata TEXT,
        reason varchar(255),
        token varchar(255) not null unique,
        token_type varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_locations (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        event_id bigint,
        id bigserial not null,
        province_id bigint,
        region_id bigint,
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table events (
        daily_spin_limit integer,
        default_win_probability float(53),
        initial_spins integer,
        status integer not null,
        created_at timestamp(6),
        end_time timestamp(6),
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table golden_hours (
        daily_limit integer,
        points_multiplier float(53),
        status integer not null,
        total_uses integer,
        win_probability float(53),
        win_probability_multiplier float(53),
        created_at timestamp(6),
        end_time timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        start_time timestamp(6),
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_events (
        available_spins integer,
        daily_spin_count integer,
        status integer not null,
        total_points integer,
        total_spins integer,
        total_wins integer,
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        participant_id bigint,
        updated_at timestamp(6),
        version bigint,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table participant_roles (
        participant_id bigint not null,
        role_id bigint not null,
        primary key (participant_id, role_id)
    );

    create table participants (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        province_id bigint,
        updated_at timestamp(6),
        user_id bigint unique,
        version bigint,
        code varchar(255),
        created_by varchar(255),
        email varchar(255),
        metadata varchar(255),
        name varchar(255) not null,
        phone varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table provinces (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        region_id bigint,
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table regions (
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table rewards (
        daily_count integer,
        daily_limit integer,
        points integer,
        points_required integer,
        remaining_quantity integer,
        status integer not null,
        total_quantity integer,
        win_probability float(53),
        created_at timestamp(6),
        event_location_id bigint,
        id bigserial not null,
        updated_at timestamp(6),
        valid_from timestamp(6),
        valid_until timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table roles (
        priority integer,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        updated_at timestamp(6),
        version bigint,
        code varchar(255) unique,
        created_by varchar(255),
        description varchar(255),
        metadata varchar(255),
        name varchar(255) unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table spin_histories (
        points_earned integer,
        points_spent integer,
        status integer not null,
        win boolean,
        created_at timestamp(6),
        event_location_id bigint,
        golden_hour_id bigint,
        id bigserial not null,
        participant_id bigint,
        reward_id bigint,
        timestamp timestamp(6),
        updated_at timestamp(6),
        version bigint,
        created_by varchar(255),
        metadata varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    );

    create table users (
        account_locked boolean,
        account_non_expired boolean,
        account_non_locked boolean,
        credentials_non_expired boolean,
        failed_attempts integer,
        password_expired boolean,
        status integer not null,
        created_at timestamp(6),
        id bigserial not null,
        locked_until timestamp(6),
        updated_at timestamp(6),
        version bigint,
        created_by varchar(255),
        email varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        metadata varchar(255),
        password varchar(255),
        phone_number varchar(255),
        position varchar(255),
        refresh_token varchar(255),
        updated_by varchar(255),
        username varchar(255) unique,
        primary key (id)
    );

    alter table if exists blacklisted_tokens 
       add constraint FKt7v2sldreoy4pqla7y6kd7uu 
       foreign key (user_id) 
       references users;

    alter table if exists event_locations 
       add constraint FKdquu070vft3scg2w79xy9secn 
       foreign key (event_id) 
       references events;

    alter table if exists event_locations 
       add constraint FKoputwo3ch1tbxhv4mp5m0p2gc 
       foreign key (province_id) 
       references provinces;

    alter table if exists event_locations 
       add constraint FKs41txxkfquf582772ahkwlub2 
       foreign key (region_id) 
       references regions;

    alter table if exists golden_hours 
       add constraint FKiextogf8tkm1q3gu9vblyswnq 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKd015ah4sdlr6ry5rx5nvnu6qa 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists participant_events 
       add constraint FKjjitit5lfpkqyk3nvwel8xgf 
       foreign key (participant_id) 
       references participants;

    alter table if exists participant_roles 
       add constraint FK70v5tkbsbn1o6xl1c22b4owtn 
       foreign key (role_id) 
       references roles;

    alter table if exists participant_roles 
       add constraint FK5ub8a1wn0n40afuju04u13qlh 
       foreign key (participant_id) 
       references participants;

    alter table if exists participants 
       add constraint FKapubofdx7bicmjxnngjaf4mcj 
       foreign key (province_id) 
       references provinces;

    alter table if exists participants 
       add constraint FKghixrahoj1s8cloinfx8lyeqa 
       foreign key (user_id) 
       references users;

    alter table if exists provinces 
       add constraint FKr52p9hvmia0r4042b4s4h6qil 
       foreign key (region_id) 
       references regions;

    alter table if exists rewards 
       add constraint FK465nimj9nfk08cul0s94bu79n 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKn5ghsrn4a8u3tve4lpbat9cf1 
       foreign key (event_location_id) 
       references event_locations;

    alter table if exists spin_histories 
       add constraint FKcje0w53l1u9ctyuxv36ufpmmu 
       foreign key (golden_hour_id) 
       references golden_hours;

    alter table if exists spin_histories 
       add constraint FKqyc5uaa3ar8vpbrbom3tspk2x 
       foreign key (participant_id) 
       references participants;

    alter table if exists spin_histories 
       add constraint FKatkkxrtpf21fsnr98awyel68c 
       foreign key (reward_id) 
       references rewards;

    alter table if exists user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table if exists user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;
