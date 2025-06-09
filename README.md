# java-filmorate
Template repository for Filmorate project.

![Database schema](/schema.png)

Примеры запросов

-- получение всех фильмов
SELECT *
FROM films;

-- получение всех пользователей
SELECT *
FROM users;

-- получение N наиболее популярных фильмов (в данном случае 10)
SELECT *
FROM films f 
ORDER BY rating DESC
LIMIT 10;

-- получение общих друзей (в данном случае пользователей с id = 1 и id = 5)
SELECT *
FROM users u 
WHERE u.user_id IN (
SELECT u.user_id
	FROM users u 
	JOIN friends f ON u.user_id = f.friend_id
	WHERE f.user_id = 1
INTERSECT
SELECT u.user_id
	FROM users u 
	JOIN friends f ON u.user_id = f.friend_id
	WHERE f.user_id = 5
);
