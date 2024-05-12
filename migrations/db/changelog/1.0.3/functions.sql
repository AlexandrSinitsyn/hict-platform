--liquibase formatted sql

--changeset AlexSin:7 splitStatements:false
--User creation function

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
$$ language plpgsql ;

--rollback drop function new_user
