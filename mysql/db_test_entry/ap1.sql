-- Adminer 4.8.1 MySQL 5.7.37 dump

SET NAMES utf8;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;
SET sql_mode = 'NO_AUTO_VALUE_ON_ZERO';

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS `ap1`;
CREATE DATABASE `ap1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `ap1`;

CREATE TABLE `item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `price` double NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sales` int(11) NOT NULL DEFAULT '0',
  `img_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `item` (`id`, `title`, `price`, `description`, `sales`, `img_url`) VALUES
(7,	'iPad Pro 11',	5749,	'【2021新款享24期免息】Apple/苹果 11 英寸 iPad Pro 2020款苹果平板电脑全面屏A12Z/M1芯片支持妙控键盘',	109,	'https://img.alicdn.com/imgextra/i4/2200724907121/O1CN01vSUGOZ22TSzELlw4h_!!2200724907121.jpg'),
(8,	'HUAWEI MatePad Pro 5G',	8599,	'huawei matepad pro 5g  huawei matepad pro 5g 重 构 创 造 力 麒麟990 5G SoC 芯片丨华为分享1丨绚丽全面屏2',	121,	'https://img.alicdn.com/imgextra/https://img.alicdn.com/imgextra/i2/858042828/O1CN01z9emsv1WlGkiiqDLo_!!858042828.jpg_430x430q90.jpg');

CREATE TABLE `item_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `stock` int(11) NOT NULL DEFAULT '0',
  `item_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `item_stock` (`id`, `stock`, `item_id`) VALUES
(7,	92,	7),
(8,	78,	8);

CREATE TABLE `order_info` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT '0',
  `item_id` int(11) NOT NULL DEFAULT '0',
  `item_price` double NOT NULL DEFAULT '0',
  `amount` int(11) NOT NULL DEFAULT '0',
  `order_price` double NOT NULL DEFAULT '0',
  `promo_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `promo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `promo_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `end_date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `item_id` int(11) NOT NULL DEFAULT '0',
  `promo_item_price` double NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `promo` (`id`, `promo_name`, `start_date`, `end_date`, `item_id`, `promo_item_price`) VALUES
(1,	'iPad pro 11抢购活动',	'2021-05-28 10:37:00',	'2023-06-01 23:59:59',	7,	100),
(2,	'HUAWEI MatePad Pro 5G 促销活动',	'2022-07-25 00:00:00',	'2023-07-25 00:00:00',	8,	7588);

CREATE TABLE `sequence_info` (
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `current_value` int(11) NOT NULL DEFAULT '0',
  `step` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `sequence_info` (`name`, `current_value`, `step`) VALUES
('order_info',	44,	1);

CREATE TABLE `stock_log` (
  `stock_log_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_id` int(11) NOT NULL DEFAULT '0',
  `amount` int(11) NOT NULL DEFAULT '0',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '// 1.表示初始状态, 2.表示下单扣减库存成功, 3.表示下单回滚',
  PRIMARY KEY (`stock_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `user_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `gender` tinyint(4) NOT NULL COMMENT '//1代表男性，2代表女性',
  `age` int(11) NOT NULL,
  `telphone` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `register_mode` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '//byphone,bywechat,byalipay',
  `third_party_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `telphone)unique_index` (`telphone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `user_info` (`id`, `name`, `gender`, `age`, `telphone`, `register_mode`, `third_party_id`) VALUES
(12,	'abietic',	49,	24,	'13009258200',	'byphone',	''),
(14,	'aaaa',	49,	11,	'456789',	'byphone',	''),
(15,	'test',	48,	22,	'123456',	'byphone',	''),
(16,	'testme',	49,	11,	'789456',	'byphone',	'');

CREATE TABLE `user_password` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `encrpt_password` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `user_password` (`id`, `encrpt_password`, `user_id`) VALUES
(4,	'ed7ec96deaee0655f8e82753b4529135',	12),
(6,	'ed7ec96deaee0655f8e82753b4529135',	14),
(7,	'e10adc3949ba59abbe56e057f20f883e',	15),
(8,	'71b3b26aaa319e0cdf6fdb8429c112b0',	16);

CREATE TABLE `user_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `role` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `user_role` (`id`, `user_id`, `role`) VALUES
(1,	12,	'ROLE_ADMIN'),
(2,	14,	'ROLE_ADMIN'),
(3,	15,	'ROLE_ADMIN'),
(4,	16,	'ROLE_USER');

-- 2022-09-19 17:01:08