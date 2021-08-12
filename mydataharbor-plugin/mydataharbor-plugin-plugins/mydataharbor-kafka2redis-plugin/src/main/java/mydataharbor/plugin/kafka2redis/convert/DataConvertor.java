/**
 *
 *    Copyright 2021 徐浪 1053618636@qq.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package mydataharbor.plugin.kafka2redis.convert;

import mydataharbor.plugin.kafka2redis.protocal.StringKeyValueKafkaRedisProtocal;
import mydataharbor.IDataConvertor;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.sink.redis.entity.StringKeyValue;

/**
 * @auth xulang
 * @Date 2021/5/6
 **/

public class DataConvertor implements IDataConvertor<StringKeyValueKafkaRedisProtocal, StringKeyValue, BaseSettingContext> {
  @Override
  public StringKeyValue convert(StringKeyValueKafkaRedisProtocal record, BaseSettingContext settingContext) throws ResetException {
    return StringKeyValue.builder().key(record.getKey()).value(record.getValue()).build();
  }
}