USE `FrissDB`;
DROP procedure IF EXISTS `usp_InsertUserMasterDetails`;

DELIMITER $$
USE `FrissDB`$$
CREATE  PROCEDURE `usp_InsertUserMasterDetails`(
	InputUserName VARCHAR(255)
	,InputUserPassword VARCHAR(255)
	,InputEmailName VARCHAR (255)
	,InputContactNumber VARCHAR(50)
	,InputDOB DATE
	,InputFirstName VARCHAR(50)
	,InputLastName VARCHAR(50)
	,InputRegistrationDateTime DATETIME
    ,InputContactNumberVerificationCode VARCHAR(50)
	,InputEmailVerificationCode VARCHAR(50)
    ,out IsError INTEGER 
    ,out UserID BIGINT
    
)
BEGIN

/**********************************************************************************************
Authors Name : Anvesh Patel
Created Date : 2015-05-14
Description  : This stored procedure used to insert user master details. 
***********************************************************************************************
										Revision History
-----------------------------------------------------------------------------------------------
Revision Number             ChangedBy              Revision Date              Change Descrption
-----------------------------------------------------------------------------------------------
***********************************************************************************************/

	DECLARE EmailFPart VARCHAR(255);
	DECLARE EmailMPart VARCHAR(255);
	DECLARE EmailLPart VARCHAR(255);
    DECLARE EmailDID INT;
	
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET IsError=1;    

	SET EmailFPart = 
	(
		SELECT SUBSTRING(InputEmailName,1,(POSITION('@' IN InputEmailName) -1))
	);
	
	SET EmailMPart =
	(
		SELECT SUBSTRING(
					 SUBSTRING(InputEmailName,(POSITION('@' IN InputEmailName) + 1))
					,1
					,POSITION('.' IN SUBSTRING(InputEmailName,(POSITION('@' IN InputEmailName))))-2
				)
	);
	
	SET EmailLPart =
	(
		SELECT SUBSTRING((SUBSTRING(InputEmailName,(POSITION('@' IN InputEmailName) + 1)))
			,POSITION('.' IN SUBSTRING(InputEmailName,(POSITION('@' IN InputEmailName) + 1)))+1)
	);

	SET EmailDID = (SELECT EmailDomainID FROM FrissDB.tbl_emails WHERE LOWER(EmailDomainName) = LOWER(EmailMPart) AND LOWER(EmailExtension)=LOWER(EmailLPart));
    
    IF (EmailDID IS NULL) THEN
    
		INSERT INTO FrissDB.tbl_emails
        (
			EmailDomainName
            ,EmailExtension
        )
        VALUES 
        (
			EmailMPart
            ,EmailLPart
        );
        
        SET EmailDID = (SELECT EmailDomainID FROM FrissDB.tbl_emails WHERE LOWER(EmailDomainName) = LOWER(EmailMPart) AND LOWER(EmailExtension)=LOWER(EmailLPart));

    END IF;

	INSERT INTO FrissDB.tbl_users
	(
		UserName 
		,UserPassword 
		,EmailName 
        ,EmailDomainID
		,ContactNumber 
		,DOB 
		,FirstName 
		,LastName 
		,RegistrationDateTime
		,ContactNumberVerificationCode
		,EmailVerificationCode        
		
	)
	VALUES
	(
        InputUserName 
		,MD5(InputUserPassword)
		,InputEmailName
        ,EmailDID
		,InputContactNumber 
		,InputDOB 
		,InputFirstName
		,InputLastName 
		,InputRegistrationDateTime
		,InputContactNumberVerificationCode
		,InputEmailVerificationCode            
	);

	SET UserID=(SELECT LAST_INSERT_ID());
    
   END$$

DELIMITER ;

