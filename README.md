# REST XDI

A basic way for web apps written in other programming languages to perform some operations on XDI graphs using the XDI reference implementation classes. 

## Warning

This is a very rough, proof of concept first cut.

## Requirements

`xdi2` https://github.com/projectdanube/xdi2

Git clone `xdi2`, run `mvn install` to install `xdi2` into your local repository.

Project also uses:
* [Git-Flow branching model](https://github.com/nvie/gitflow)
* [Bootspring platform](http://projects.spring.io/spring-boot) 


## Building

This project uses Gradle. Run `./gradlew bootRun` to build and run.

## Examples

### Signatures

Send a POST request to `/signature/sign/=alice` with the request body a serialized XDI graph:

EG.

`curl -X POST -d @sample.xdi http://0.0.0.0:8080/signature/sign/=alice`


	//=alice
	=alice//<#age>
	=alice//<#photo>
	=alice//#address
	=alice<#age>//<$t>
	=alice<#age>&/&/33
	=alice<#age><$t>&/&/"2010-10-10T11:12:13Z"
	=alice<#photo>&/&/"R0lGODdhMAAwAPAAAAAAAP///ywAAAqzOfkVJ+5YiUqrXF5Y5lKh/DeuNcP5yLWGsEbtLiOSp"
	=alice#address<#street>&/&/"Street 12"
	=alice#address<#postal><#code>&/&/"01234"
	=alice#address<#city>&/&/"MyCity"
	=alice#address<#state>&/&/"XX"
	=alice#address<#country>&/&/"US"
	=alice/#friend/=bob
	=alice/#friend/=charlie

