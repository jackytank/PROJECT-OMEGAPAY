create database BankAppDB
go
use BankAppDB
go
-- Create table

create table [User_Login](
	OmegaAccount nvarchar(255) primary key,
	Username nvarchar(255) not null,
	Password nvarchar(255) not null
)

create table [User_Detail](
	OmegaAccount nvarchar(255) primary key,
	FirstName nvarchar(50) not null,
	LastName nvarchar(50) not null,
	Email nvarchar(255) not null,
	Phone nvarchar(13) not null,
	Gender bit not null,
	Birthday date not null,
	Address nvarchar(255) not null,
	DayCreated date not null,
	Status nvarchar(50) not null,
	Photo nvarchar(255) not null,
	OmegaBalance float not null,
)

create table [Card](
	CardID int identity(1,1) primary key,
	OmegaAccount nvarchar(255) null,
	CardNumber varchar(16) not null,
	PIN varchar(6) not null,
	ExpirationDate date not null,
	CardHolderName nvarchar(255) not null,
	BillingAddress nvarchar(255) not null,
	CardBalance float not null,
	CardName nvarchar(100) not null
)
create table [Transaction](
	TransactionID int identity(1,1) primary key,
	TransactionDate date not null,
	FromAccount nvarchar(255) not null,
	ToAccount nvarchar(255) not null,
	Amount float not null,
	Note nvarchar(255) null,
)
go

-- Add foreign keys
ALTER TABLE [dbo].[User_Detail]  WITH CHECK ADD  CONSTRAINT [FK_UserDetail_UserLogin] FOREIGN KEY([OmegaAccount])
REFERENCES [dbo].[User_Login] ([OmegaAccount])
GO
ALTER TABLE [dbo].[User_Detail] CHECK CONSTRAINT [FK_UserDetail_UserLogin]
GO
ALTER TABLE [dbo].[Card]  WITH CHECK ADD  CONSTRAINT [FK_Card_UserLogin] FOREIGN KEY([OmegaAccount])
REFERENCES [dbo].[User_Login] ([OmegaAccount])
GO
ALTER TABLE [dbo].[Card] CHECK CONSTRAINT [FK_Card_UserLogin]
GO

-- Insert demo value
INSERT [dbo].[User_Login] ([OmegaAccount], [Username], [Password]) VALUES (N'111299893443', N'Le Ninh Tuan', N'123456')
INSERT [dbo].[User_Login] ([OmegaAccount], [Username], [Password]) VALUES (N'690078902233', N'To Minh Tri', N'123456')
INSERT [dbo].[User_Login] ([OmegaAccount], [Username], [Password]) VALUES (N'456799331248', N'Nguyen Quang Teo', N'123456')
GO

INSERT [dbo].[User_Detail] ([OmegaAccount], [FirstName], [LastName], [Email], [Phone], [Gender], [Birthday], [Address], [DayCreated], [Status], [Photo], [OmegaBalance]) VALUES (N'690078902233', N'Tri', N'To Minh', N'abc@gmai.com', N'0331873382', 0, CAST(N'2005-03-01' AS Date), N'No.339 St. 1A, Long An', CAST(N'2021-11-14' AS Date), N'Platinum', N'elonmusk.png',304000)
GO

SET IDENTITY_INSERT [dbo].[Card] ON 

INSERT [dbo].[Card] ([CardID], [OmegaAccount], [CardNumber], [PIN], [ExpirationDate], [CardHolderName], [BillingAddress], [CardBalance], [CardName]) VALUES (1, NULL, N'650492834958', N'445566', CAST(N'2025-11-01' AS Date), N'To Minh Tri', N'No.69 St. Truong Chinh, HCM', 100000000, N'Sacombank')
INSERT [dbo].[Card] ([CardID], [OmegaAccount], [CardNumber], [PIN], [ExpirationDate], [CardHolderName], [BillingAddress], [CardBalance], [CardName]) VALUES (2, NULL, N'384723451121', N'889911', CAST(N'2026-12-01' AS Date), N'Nguyen Quang Teo', N'No.112 St. CMT8, HCM', 35000000, N'Techcombank')
INSERT [dbo].[Card] ([CardID], [OmegaAccount], [CardNumber], [PIN], [ExpirationDate], [CardHolderName], [BillingAddress], [CardBalance], [CardName]) VALUES (3, NULL, N'394522349981', N'221188', CAST(N'2023-06-01' AS Date), N'To Minh Tri', N'No.69 St. Truong Chinh, HCM', 24500000, N'MBBank')
SET IDENTITY_INSERT [dbo].[Card] OFF
GO
SET IDENTITY_INSERT [dbo].[Transaction] ON 

INSERT [dbo].[Transaction] ([TransactionID], [TransactionDate], [FromAccount], [ToAccount], [Amount], [Note]) VALUES (1, CAST(N'2021-11-15' AS Date), N'111299893443', N'690078902233', 30000000, N'Tra no cuoi nam')
INSERT [dbo].[Transaction] ([TransactionID], [TransactionDate], [FromAccount], [ToAccount], [Amount], [Note]) VALUES (6, CAST(N'2021-11-18' AS Date), N'456799331248', N'456799331248', 500000, N'Li xi tet')
SET IDENTITY_INSERT [dbo].[Transaction] OFF
GO




