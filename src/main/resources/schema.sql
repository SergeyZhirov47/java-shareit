DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

/*
-- ToDo
-- ссылки на request не хватает
-- ToDo
-- при удалении пользователя что будет с owner_id ?
CREATE TABLE IF NOT EXISTS items
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    is_available boolean NOT NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT pk_items PRIMARY KEY (id),
    CONSTRAINT fk_users_items FOREIGN KEY(owner_id) REFERENCES (id) users
);
*/
