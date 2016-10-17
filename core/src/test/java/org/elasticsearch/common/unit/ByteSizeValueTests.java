begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.unit
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|StreamInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_class
DECL|class|ByteSizeValueTests
specifier|public
class|class
name|ByteSizeValueTests
extends|extends
name|ESTestCase
block|{
DECL|method|testActualPeta
specifier|public
name|void
name|testActualPeta
parameter_list|()
block|{
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|4
argument_list|,
name|ByteSizeUnit
operator|.
name|PB
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4503599627370496L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testActualTera
specifier|public
name|void
name|testActualTera
parameter_list|()
block|{
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|4
argument_list|,
name|ByteSizeUnit
operator|.
name|TB
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4398046511104L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testActual
specifier|public
name|void
name|testActual
parameter_list|()
block|{
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|4
argument_list|,
name|ByteSizeUnit
operator|.
name|GB
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4294967296L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimple
specifier|public
name|void
name|testSimple
parameter_list|()
block|{
name|assertThat
argument_list|(
name|ByteSizeUnit
operator|.
name|BYTES
operator|.
name|toBytes
argument_list|(
literal|10
argument_list|)
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeUnit
operator|.
name|KB
operator|.
name|toKB
argument_list|(
literal|10
argument_list|)
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
operator|.
name|getKb
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeUnit
operator|.
name|MB
operator|.
name|toMB
argument_list|(
literal|10
argument_list|)
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
operator|.
name|getMb
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeUnit
operator|.
name|GB
operator|.
name|toGB
argument_list|(
literal|10
argument_list|)
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|GB
argument_list|)
operator|.
name|getGb
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeUnit
operator|.
name|TB
operator|.
name|toTB
argument_list|(
literal|10
argument_list|)
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|TB
argument_list|)
operator|.
name|getTb
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeUnit
operator|.
name|PB
operator|.
name|toPB
argument_list|(
literal|10
argument_list|)
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|PB
argument_list|)
operator|.
name|getPb
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEquality
specifier|public
name|void
name|testEquality
parameter_list|()
block|{
name|String
index|[]
name|equalValues
init|=
operator|new
name|String
index|[]
block|{
literal|"1GB"
block|,
literal|"1024MB"
block|,
literal|"1048576KB"
block|,
literal|"1073741824B"
block|}
decl_stmt|;
name|ByteSizeValue
name|value1
init|=
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
name|randomFrom
argument_list|(
name|equalValues
argument_list|)
argument_list|,
literal|"equalTest"
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|value2
init|=
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
name|randomFrom
argument_list|(
name|equalValues
argument_list|)
argument_list|,
literal|"equalTest"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|value1
argument_list|,
name|equalTo
argument_list|(
name|value2
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testToString
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|assertThat
argument_list|(
literal|"10b"
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1.5kb"
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
call|(
name|long
call|)
argument_list|(
literal|1024
operator|*
literal|1.5
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1.5mb"
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
call|(
name|long
call|)
argument_list|(
literal|1024
operator|*
literal|1.5
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1.5gb"
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
call|(
name|long
call|)
argument_list|(
literal|1024
operator|*
literal|1.5
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1.5tb"
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
call|(
name|long
call|)
argument_list|(
literal|1024
operator|*
literal|1.5
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|GB
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1.5pb"
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
call|(
name|long
call|)
argument_list|(
literal|1024
operator|*
literal|1.5
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|TB
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1536pb"
argument_list|,
name|is
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
call|(
name|long
call|)
argument_list|(
literal|1024
operator|*
literal|1.5
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|PB
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsing
specifier|public
name|void
name|testParsing
parameter_list|()
block|{
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"42PB"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42pb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"42 PB"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42pb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"42pb"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42pb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"42 pb"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42pb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"42P"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42pb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"42 P"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42pb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"42p"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42pb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"42 p"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"42pb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"54TB"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"54tb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"54 TB"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"54tb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"54tb"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"54tb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"54 tb"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"54tb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"54T"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"54tb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"54 T"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"54tb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"54t"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"54tb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"54 t"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"54tb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12GB"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12gb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12 GB"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12gb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12gb"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12gb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12 gb"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12gb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12G"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12gb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12 G"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12gb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12g"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12gb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12 g"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12gb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12M"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12mb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12 M"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12mb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12m"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12mb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"12 m"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"12mb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"23KB"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"23kb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"23 KB"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"23kb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"23kb"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"23kb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"23 kb"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"23kb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"23K"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"23kb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"23 K"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"23kb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"23k"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"23kb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"23 k"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"23kb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"1B"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"1b"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"1 B"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"1b"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"1b"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"1b"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"1 b"
argument_list|,
literal|"testParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"1b"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailOnMissingUnits
specifier|public
name|void
name|testFailOnMissingUnits
parameter_list|()
block|{
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|ElasticsearchParseException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"23"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"failed to parse setting [test]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailOnUnknownUnits
specifier|public
name|void
name|testFailOnUnknownUnits
parameter_list|()
block|{
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|ElasticsearchParseException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"23jw"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"failed to parse setting [test]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailOnEmptyParsing
specifier|public
name|void
name|testFailOnEmptyParsing
parameter_list|()
block|{
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|ElasticsearchParseException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|""
argument_list|,
literal|"emptyParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"23kb"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"failed to parse setting [emptyParsing]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailOnEmptyNumberParsing
specifier|public
name|void
name|testFailOnEmptyNumberParsing
parameter_list|()
block|{
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|ElasticsearchParseException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|assertThat
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"g"
argument_list|,
literal|"emptyNumberParsing"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"23b"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"failed to parse [g]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoDotsAllowed
specifier|public
name|void
name|testNoDotsAllowed
parameter_list|()
block|{
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|ElasticsearchParseException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"42b."
argument_list|,
literal|null
argument_list|,
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"failed to parse setting [test]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompareEquality
specifier|public
name|void
name|testCompareEquality
parameter_list|()
block|{
name|long
name|firstRandom
init|=
name|randomPositiveLong
argument_list|()
decl_stmt|;
name|ByteSizeUnit
name|randomUnit
init|=
name|randomFrom
argument_list|(
name|ByteSizeUnit
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|firstByteValue
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|firstRandom
argument_list|,
name|randomUnit
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|secondByteValue
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|firstRandom
argument_list|,
name|randomUnit
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|firstByteValue
operator|.
name|compareTo
argument_list|(
name|secondByteValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompareValue
specifier|public
name|void
name|testCompareValue
parameter_list|()
block|{
name|long
name|firstRandom
init|=
name|randomPositiveLong
argument_list|()
decl_stmt|;
name|long
name|secondRandom
init|=
name|randomValueOtherThan
argument_list|(
name|firstRandom
argument_list|,
name|ESTestCase
operator|::
name|randomPositiveLong
argument_list|)
decl_stmt|;
name|ByteSizeUnit
name|unit
init|=
name|randomFrom
argument_list|(
name|ByteSizeUnit
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|firstByteValue
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|firstRandom
argument_list|,
name|unit
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|secondByteValue
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|secondRandom
argument_list|,
name|unit
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|firstRandom
operator|>
name|secondRandom
argument_list|,
name|firstByteValue
operator|.
name|compareTo
argument_list|(
name|secondByteValue
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|secondRandom
operator|>
name|firstRandom
argument_list|,
name|secondByteValue
operator|.
name|compareTo
argument_list|(
name|firstByteValue
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompareUnits
specifier|public
name|void
name|testCompareUnits
parameter_list|()
block|{
name|long
name|number
init|=
name|randomPositiveLong
argument_list|()
decl_stmt|;
name|ByteSizeUnit
name|randomUnit
init|=
name|randomValueOtherThan
argument_list|(
name|ByteSizeUnit
operator|.
name|PB
argument_list|,
parameter_list|()
lambda|->
name|randomFrom
argument_list|(
name|ByteSizeUnit
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|firstByteValue
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|number
argument_list|,
name|randomUnit
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|secondByteValue
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|number
argument_list|,
name|ByteSizeUnit
operator|.
name|PB
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|firstByteValue
operator|.
name|compareTo
argument_list|(
name|secondByteValue
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|secondByteValue
operator|.
name|compareTo
argument_list|(
name|firstByteValue
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
DECL|method|testEdgeCompare
specifier|public
name|void
name|testEdgeCompare
parameter_list|()
block|{
name|ByteSizeValue
name|maxLongValuePB
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|ByteSizeUnit
operator|.
name|PB
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|maxLongValueB
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|maxLongValuePB
operator|.
name|compareTo
argument_list|(
name|maxLongValueB
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
DECL|method|testConversionHashCode
specifier|public
name|void
name|testConversionHashCode
parameter_list|()
block|{
name|ByteSizeValue
name|firstValue
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|GB
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|secondValue
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|firstValue
operator|.
name|getBytes
argument_list|()
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|firstValue
operator|.
name|hashCode
argument_list|()
argument_list|,
name|secondValue
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|ByteSizeValue
name|byteSizeValue
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|randomPositiveLong
argument_list|()
argument_list|,
name|randomFrom
argument_list|(
name|ByteSizeUnit
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
try|try
init|(
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
name|byteSizeValue
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
try|try
init|(
name|StreamInput
name|in
init|=
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
init|)
block|{
name|ByteSizeValue
name|deserializedByteSizeValue
init|=
operator|new
name|ByteSizeValue
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|byteSizeValue
operator|.
name|getBytes
argument_list|()
argument_list|,
name|deserializedByteSizeValue
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

