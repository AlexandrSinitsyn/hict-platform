--liquibase formatted sql

--changeset AlexSin:7 splitStatements:false
--Functions

create or replace function new_user(username varchar(100),
                                    login varchar(64),
                                    email varchar(255),
                                    password varchar(32))
    returns setof users as
$$
begin
    if exists (select 1
               from users
               where users.login = new_user.login
                  or users.email = new_user.email) then
        return query table users limit 0;
        return;
    end if;

    insert into users (username, login, email, password)
    values (new_user.username,
            new_user.login,
            new_user.email,
            crypt(new_user.password, gen_salt('bf')));

    return query select *
                 from users
                 where users.login = new_user.login;
end;
$$ language plpgsql;

create or replace function save_file(filename varchar(256),
                                     sequence_level varchar(10),
                                     file_size bigint)
    returns setof files as
$$
declare
    new_id uuid;
begin
    new_id := gen_random_uuid();

    insert into files (file_id, filename, sequence_level, file_size)
    values (new_id, save_file.filename, cast(save_file.sequence_level as sequence_level_type), save_file.file_size);

    return query select * from files where files.file_id = new_id;
end;
$$ language plpgsql;

--rollback drop function new_user
