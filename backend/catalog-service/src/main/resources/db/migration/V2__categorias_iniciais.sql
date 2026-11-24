INSERT INTO categorias (nome) VALUES
    ('Desenvolvimento web'),
    ('Desenvolvimento mobile'),
    ('Desenvolvimento de software'),
    ('Infraestrutura e redes'),
    ('Suporte técnico'),
    ('Design e experiência do usuário'),
    ('Consultoria em tecnologia')
ON CONFLICT DO NOTHING;
