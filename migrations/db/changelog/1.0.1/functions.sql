--liquibase formatted sql

--changeset AlexSin:3
--User creation function

create or replace function new_user(username varchar(100),
                                    login varchar(64),
                                    email varchar(255),
                                    password varchar(32),
                                    role smallint)
    returns setof users as
$$
declare
    user_salt varchar(30);
begin
    if exists (select 1
               from users
               where users.login = new_user.login
                  or users.email = new_user.email) then
        return query (select);
    end if;

    user_salt := gen_salt('bf');

    insert into users (username, login, email, role, salt, password_sha)
    values (new_user.username,
        new_user.login,
        new_user.email,
        new_user.role,
        user_salt,
        crypt(new_user.password, user_salt));

    return query select *
                 from users
                 where users.login = new_user.login;
end;
$$ language plpgsql ;

--rollback drop function new_user
