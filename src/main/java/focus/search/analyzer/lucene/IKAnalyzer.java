/**
 * IK 中文分词  版本 5.0.1
 * IK Analyzer release 5.0.1
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 */
package focus.search.analyzer.lucene;

import focus.search.analyzer.dic.Dictionary;
import org.apache.lucene.analysis.Analyzer;

import java.io.Reader;
import java.io.Serializable;

public final class IKAnalyzer extends Analyzer implements Serializable {

    private IKTokenizer _IKTokenizer;

    public IKAnalyzer() {
        super();
        // 初始化词典单例
        Dictionary.initial();
    }

    /**
     * 重载Analyzer接口，构造分词组件
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName, final Reader in) {
        _IKTokenizer = new IKTokenizer(in);
        return new TokenStreamComponents(_IKTokenizer);
    }

}
