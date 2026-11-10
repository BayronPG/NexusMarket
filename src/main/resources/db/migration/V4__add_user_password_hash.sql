-- ---------------------------------------------------------------------------
-- V4 - Password hash for authentication
-- ---------------------------------------------------------------------------
-- Solo se guarda el hash BCrypt (60 caracteres), nunca la contrasena.
-- La columna es NULL a proposito: los usuarios existentes no reciben una
-- contrasena compartida y no pueden iniciar sesion hasta que un proceso de
-- aprovisionamiento (administrativo o de demostracion) les asigne un hash.
-- V1, V2 y V3 no se modifican.
-- ---------------------------------------------------------------------------

ALTER TABLE users ADD COLUMN password_hash VARCHAR(60);
