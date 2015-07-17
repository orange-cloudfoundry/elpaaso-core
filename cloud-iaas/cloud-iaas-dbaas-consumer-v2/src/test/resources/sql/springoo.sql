drop table if exists ARTMAR;
drop table if exists ARTICLE;
drop table if exists MARKET;
drop table if exists CATALOG;

--
-- Name: market; Type: TABLE; Schema: public; 
--

CREATE TABLE market (
    idmar serial NOT NULL,
    name varchar(30) unique,
    version integer NOT NULL default 1,
    description varchar(2000),
    primary key (idmar)
);

--
-- Name: catalog; Type: TABLE; Schema: public;
--

CREATE TABLE catalog (
    idcat serial NOT NULL,
    name varchar(30) unique,
    version integer NOT NULL default 1,
    description varchar(2000),
    primary key (idcat)
);


--
-- Name: article; Type: TABLE; Schema: public;
--

CREATE TABLE article (
    idart serial NOT NULL,
    name varchar(30) unique,
    version integer NOT NULL default 1,
    availdate timestamp without time zone,
    articletype varchar(1) NOT NULL,
    description varchar(2000),
    color varchar(10),
    price real,
    charge real,
    options varchar(255),
    idcat integer NOT NULL, 
    primary key (idart)
);

--
-- Name: artmar; Type: TABLE; Schema: public; Owner: beal6226; Tablespace:
--

CREATE TABLE artmar (
    idart integer NOT NULL,
    idmar integer NOT NULL
);

ALTER TABLE ONLY article
    ADD CONSTRAINT article_fkey_catalog FOREIGN KEY (idcat) REFERENCES catalog(idcat);

ALTER TABLE ONLY artmar
    ADD CONSTRAINT artmar_fkey_article FOREIGN KEY (idart) REFERENCES article(idart);

ALTER TABLE ONLY artmar
    ADD CONSTRAINT artmar_fkey_market FOREIGN KEY (idmar) REFERENCES market(idmar);

delete from artmar;
delete from article;
delete from market;
delete from catalog;
SELECT pg_catalog.setval('market_idmar_seq', 1, false);
SELECT pg_catalog.setval('catalog_idcat_seq', 1, false);
SELECT pg_catalog.setval('article_idart_seq', 1, false);

insert into market(name, description) values ('market1','description of market number 1 on postgresql ê@éi');
insert into market(name, description) values ('market2','description of market number 2 on postgresql');
insert into market(name, description) values ('market3','description of market number 3 on postgresql');

insert into catalog(name, description) values ('catalog1','description of catalog number 1 on postgresql');
insert into catalog(name, description) values ('catalog2','description of catalog number 2 on postgresql');
insert into catalog(name, description) values ('catalog3','description of catalog number 3 on postgresql');
insert into catalog(name, description) values ('catalog4','description of catalog number 4 on postgresql');
insert into catalog(name, description) values ('catalog5','description of catalog number 5 on postgresql');
insert into catalog(name, description) values ('catalog6','description of catalog number 6 on postgresql');

insert into article(idcat, name, description, availdate, articletype, color, price) values (1,'article1','description of article number 1 on postgresql',NOW(),'M','color1','11.3');
insert into article(idcat, name, description, availdate, articletype, color, price) values (1,'article2','description of article number 2 on postgresql',NOW(),'M','color2','11.8');
insert into article(idcat, name, description, availdate, articletype, charge, options) values (1,'article3','description of article number 3 on postgresql',NOW(),'S','12.0','options3');
insert into article(idcat, name, description, availdate, articletype, color, price) values (3,'article4','description of article number 4 on postgresql',NOW(),'M','color4','11.1');
insert into article(idcat, name, description, availdate, articletype, color, price) values (1,'article5','description of article number 5 on postgresql',NOW(),'M','color1','11.3');
insert into article(idcat, name, description, availdate, articletype, color, price) values (5,'article6','description of article number 6 on postgresql',NOW(),'M','color2','11.8');
insert into article(idcat, name, description, availdate, articletype, charge, options) values (2,'article7','description of article number 7 on postgresql',NOW(),'S','12.0','options7');
insert into article(idcat, name, description, availdate, articletype, color, price) values (1,'article8','description of article number 8 on postgresql',NOW(),'M','color4','11.1');
insert into article(idcat, name, description, availdate, articletype, color, price) values (1,'article9','description of article number 9 on postgresql',NOW(),'M','color9','11.3');
insert into article(idcat, name, description, availdate, articletype, color, price) values (1,'article10','description of article number 10 on postgresql',NOW(),'M','color10','11.8');
insert into article(idcat, name, description, availdate, articletype, color, price) values (2,'article11','description of article number 11 on postgresql',NOW(),'M','color11','11.7');
insert into article(idcat, name, description, availdate, articletype, color, price) values (3,'article12','description of article number 12 on postgresql',NOW(),'M','color12','11.1');
insert into article(idcat, name, description, availdate, articletype, color, price) values (1,'article13','description of article number 13 on postgresql',NOW(),'M','color13','11.3');


insert into artmar values (1,1);
insert into artmar values (1,2);
insert into artmar values (1,3);
insert into artmar values (2,2);
insert into artmar values (3,1);
insert into artmar values (3,3);
insert into artmar values (4,2);
insert into artmar values (5,2);
insert into artmar values (5,3);
insert into artmar values (6,1);
insert into artmar values (7,1);
insert into artmar values (8,3);
insert into artmar values (9,3);
insert into artmar values (10,1);
insert into artmar values (11,1);
insert into artmar values (12,3);
insert into artmar values (13,3);

