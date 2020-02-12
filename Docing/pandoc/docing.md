
![](Image.png)
![](Image.png)
![](Image.png)

<br>
<br>
<br>

<h1><center>My cool Report</center></h1>
<center>erzeugt am 10.10.2020 um 12:22 Uhr</center>
<center>mit Hilfe von [SysNat](https://github.com/iks-github/SysNat/wiki), [Pandoc](https://pandoc.org) und [Prince](https://www.princexml.com/)</center>

<br>
<br>
<br>



*_Inhaltsverzeichnis_*

* [1. Hauptkapitel][1. Hauptkapitel]
	* [1.1 Kapitel][1.1 Kapitel]
		* [1.1.1 Unterkapitel][1.1.1 Unterkapitel]
* [2. Hauptkapitel][2. Hauptkapitel]

<div style="page-break-after: always;"></div>

# 1. Hauptkapitel

## 1.1 Kapitel

Bla *Bla* **Bla** ***Bla***

### 1.1.1 Unterkapitel

Blub _Blub_ __Blub__ ___Blub___

# 2. Hauptkapitel



<style>
img:nth-of-type(1) {
  margin-top: 0;
  margin-left: 0;
  margin-right: 0;
  width: 100%;
}
  
img:nth-of-type(2) {
  margin-top: 0;
  float: right;
  width: 10%; 
}


/*
body {
  background: linear-gradient(45deg, grey, lightgrey);
  background-image: url(filename)
  
}
*/

/*********************  Table Settings     ***************/
table {
    table-layout:fixed;
    width:100%;
    border: solid 2px black;	 /* Tabellenrahmen */
    border-collapse: collapse;
	background-color: green;  /* overwritten below */
}

/* Gitter zwischen Zellen: 
table td { 
	border: solid 1px black;
} */

/* kein Gitter nur senkrechte Spaltentrennlinien: */
table td { 
	border-right: solid 1px black;
} 

/* alternating row color */
table tr:nth-of-type(odd) {
  background: lightgrey;
}
table tr:nth-of-type(even) {
  background: #efefef;
}

/* Pseudoheader (erste Zeile unter "first-child"): */
table tr:nth-of-type(1) {
  background: darkgrey;
  border: solid 1px black;
}

/* 
Links:
https://css-tricks.com/the-difference-between-nth-child-and-nth-of-type/ 
https://www.princexml.com/doc/
*/
/************************************************************/

</style>

|             |                     |                      | /* first child of table - not well to format */
|:-----------:|:--------------------|---------------------:|
| HEADER1     | &#160;&#160;HEADER2 | HEADER3 &#160;&#160; |
| simple      | &#160;&#160;no      | and     &#160;&#160; |
| so          | &#160;&#160;on      | no      &#160;&#160; |
| simple      | &#160;&#160;no      | and     &#160;&#160; |
| so          | &#160;&#160;on      | no      &#160;&#160; |
| simple      | &#160;&#160;no      | and     &#160;&#160; |
| so          | &#160;&#160;on      | no      &#160;&#160; |

<br>

* * *

xyz