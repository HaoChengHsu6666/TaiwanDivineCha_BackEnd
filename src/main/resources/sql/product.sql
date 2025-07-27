-- MySQL dump 10.13  Distrib 8.0.26, for Win64 (x86_64)
--
-- Host: localhost    Database: taiwan_divine_cha
-- ------------------------------------------------------
-- Server version	8.0.26

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `product_name` varchar(128) NOT NULL,
  `category` varchar(32) NOT NULL,
  `image_url` varchar(256) NOT NULL,
  `price` int NOT NULL,
  `stock` int NOT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `created_date` timestamp NOT NULL,
  `last_modified_date` timestamp NOT NULL,
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (4,'蘋果（澳洲）','FOOD','https://cdn.pixabay.com/photo/2016/11/30/15/00/apples-1872997_1280.jpg',30,10,'這是來自澳洲的蘋果！','2022-03-19 09:00:00','2022-03-22 10:00:00'),(5,'蘋果（日本北海道）','FOOD','https://cdn.pixabay.com/photo/2017/09/26/13/42/apple-2788662_1280.jpg',300,5,'這是來自日本北海道的蘋果！','2022-03-19 10:30:00','2022-03-19 10:30:00'),(6,'好吃又鮮甜的蘋果橘子','FOOD','https://cdn.pixabay.com/photo/2021/07/30/04/17/orange-6508617_1280.jpg',10,50,NULL,'2022-03-20 01:00:00','2022-03-24 07:00:00'),(7,'Toyota','CAR','https://cdn.pixabay.com/photo/2014/05/18/19/13/toyota-347288_1280.jpg',100000,5,NULL,'2022-03-20 01:20:00','2022-03-20 01:20:00'),(8,'BMW','CAR','https://cdn.pixabay.com/photo/2018/02/21/03/15/bmw-m4-3169357_1280.jpg',500000,3,'渦輪增壓，直列4缸，DOHC雙凸輪軸，16氣門','2022-03-20 04:30:00','2022-03-20 04:30:00'),(9,'Benz','CAR','https://cdn.pixabay.com/photo/2017/03/27/14/56/auto-2179220_1280.jpg',600000,0,NULL,'2022-03-21 12:10:00','2022-07-05 04:48:42'),(10,'Tesla','CAR','https://cdn.pixabay.com/photo/2021/01/15/16/49/tesla-5919764_1280.jpg',450000,1,'世界最暢銷的充電式汽車','2022-03-21 15:30:00','2022-07-05 04:48:42'),(11,'蘋果（澳洲）','FOOD','https://cdn.pixabay.com/photo/2016/11/30/15/00/apples-1872997_1280.jpg',30,10,'這是來自澳洲的蘋果！','2022-03-19 09:00:00','2022-03-22 10:00:00'),(12,'蘋果（日本北海道）','FOOD','https://cdn.pixabay.com/photo/2017/09/26/13/42/apple-2788662_1280.jpg',300,5,'這是來自日本北海道的蘋果！','2022-03-19 10:30:00','2022-03-19 10:30:00'),(13,'好吃又鮮甜的蘋果橘子','FOOD','https://cdn.pixabay.com/photo/2021/07/30/04/17/orange-6508617_1280.jpg',10,50,NULL,'2022-03-20 01:00:00','2022-03-24 07:00:00'),(14,'Toyota','CAR','https://cdn.pixabay.com/photo/2014/05/18/19/13/toyota-347288_1280.jpg',100000,5,NULL,'2022-03-20 01:20:00','2022-03-20 01:20:00'),(15,'BMW','CAR','https://cdn.pixabay.com/photo/2018/02/21/03/15/bmw-m4-3169357_1280.jpg',500000,3,'渦輪增壓，直列4缸，DOHC雙凸輪軸，16氣門','2022-03-20 04:30:00','2022-03-20 04:30:00'),(16,'Benz','CAR','https://cdn.pixabay.com/photo/2017/03/27/14/56/auto-2179220_1280.jpg',600000,2,NULL,'2022-03-21 12:10:00','2022-03-22 02:50:00'),(17,'Tesla','CAR','https://cdn.pixabay.com/photo/2021/01/15/16/49/tesla-5919764_1280.jpg',450000,5,'世界最暢銷的充電式汽車','2022-03-21 15:30:00','2022-03-21 15:30:00');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-07-05 15:01:35
