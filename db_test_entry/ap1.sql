-- Adminer 4.8.1 MySQL 5.7.37 dump

SET NAMES utf8;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;
SET sql_mode = 'NO_AUTO_VALUE_ON_ZERO';

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS `ap1`;
CREATE DATABASE `ap1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `ap1`;

DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(64) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `price` double NOT NULL,
  `description` varchar(500) COLLATE utf8_unicode_ci NOT NULL,
  `sales` int(11) NOT NULL DEFAULT '0',
  `img_url` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `item` (`id`, `title`, `price`, `description`, `sales`, `img_url`) VALUES
(7,	'iPad Pro 11',	5749,	'【2021新款享24期免息】Apple/苹果 11 英寸 iPad Pro 2020款苹果平板电脑全面屏A12Z/M1芯片支持妙控键盘',	91,	'https://img.alicdn.com/imgextra/i4/2200724907121/O1CN01vSUGOZ22TSzELlw4h_!!2200724907121.jpg'),
(8,	'HUAWEI MatePad Pro 5G',	8599,	'huawei matepad pro 5g  huawei matepad pro 5g 重 构 创 造 力 麒麟990 5G SoC 芯片丨华为分享1丨绚丽全面屏2',	102,	'https://img.alicdn.com/imgextra/https://img.alicdn.com/imgextra/i2/858042828/O1CN01z9emsv1WlGkiiqDLo_!!858042828.jpg_430x430q90.jpg');

DROP TABLE IF EXISTS `item_stock`;
CREATE TABLE `item_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `stock` int(11) NOT NULL DEFAULT '0',
  `item_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `item_stock` (`id`, `stock`, `item_id`) VALUES
(7,	214,	7),
(8,	97,	8);

DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `id` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT '0',
  `item_id` int(11) NOT NULL DEFAULT '0',
  `item_price` double NOT NULL DEFAULT '0',
  `amount` int(11) NOT NULL DEFAULT '0',
  `order_price` double NOT NULL DEFAULT '0',
  `promo_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `order_info` (`id`, `user_id`, `item_id`, `item_price`, `amount`, `order_price`, `promo_id`) VALUES
('2021052700000000',	9,	8,	5299,	1,	5299,	0),
('2021052700000100',	9,	8,	5299,	1,	5299,	0),
('2021052800000200',	9,	7,	5749,	1,	5749,	0),
('2021052800000300',	9,	8,	8599,	1,	8599,	0),
('2021052800000400',	9,	7,	100,	1,	100,	1),
('2021052900000500',	9,	7,	100,	1,	100,	1);

DROP TABLE IF EXISTS `promo`;
CREATE TABLE `promo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `promo_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `start_date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `end_date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `item_id` int(11) NOT NULL DEFAULT '0',
  `promo_item_price` double NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `promo` (`id`, `promo_name`, `start_date`, `end_date`, `item_id`, `promo_item_price`) VALUES
(1,	'iPad pro 11抢购活动',	'2021-05-28 10:37:00',	'2021-06-01 23:59:59',	7,	100);

DROP TABLE IF EXISTS `sequence_info`;
CREATE TABLE `sequence_info` (
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `current_value` int(11) NOT NULL DEFAULT '0',
  `step` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `sequence_info` (`name`, `current_value`, `step`) VALUES
('order_info',	6,	1);

DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `gender` tinyint(4) NOT NULL COMMENT '//1代表男性，2代表女性',
  `age` int(11) NOT NULL,
  `telphone` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `register_mode` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT '//byphone,bywechat,byalipay',
  `third_party_id` varchar(64) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `telphone)unique_index` (`telphone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `user_info` (`id`, `name`, `gender`, `age`, `telphone`, `register_mode`, `third_party_id`) VALUES
(1,	'manster',	1,	18,	'1249041911',	'byphone',	''),
(9,	'm',	1,	20,	'12345678911',	'byphone',	''),
(11,	'man',	1,	18,	'15012345678',	'byphone',	'');

DROP TABLE IF EXISTS `user_password`;
CREATE TABLE `user_password` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `encrpt_password` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `user_password` (`id`, `encrpt_password`, `user_id`) VALUES
(1,	'4QrcOUm6Wau+VuBX8g+IPg==',	1),
(2,	'4QrcOUm6Wau+VuBX8g+IPg==',	9),
(3,	'4QrcOUm6Wau+VuBX8g+IPg==',	11);

-- 2022-06-13 07:45:32