DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

-- ToDo
-- при удалении пользователя что будет с owner_id ?
CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    is_available boolean NOT NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT pk_items PRIMARY KEY (id),
    CONSTRAINT fk_users_items FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- ToDo
-- что будет с bookings при удалении предмета или пользователя?
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    item_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    start_booking TIMESTAMP,
    end_booking TIMESTAMP,
    status varchar NOT NULL,
    CONSTRAINT fk_items_bookings FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_users_bookings FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ToDo
-- при удалении пользователя, что будет с комментарием?
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    text VARCHAR(1000) NOT NULL,
    item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created TIMESTAMP,
    CONSTRAINT fk_items_comments FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_comments FOREIGN KEY (author_id) REFERENCES users(id)
);