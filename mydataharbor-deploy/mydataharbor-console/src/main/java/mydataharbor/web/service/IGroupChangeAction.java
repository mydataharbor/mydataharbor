package mydataharbor.web.service;

import mydataharbor.plugin.api.group.GroupInfo;

/**
 * @auth xulang
 * @Date 2021/7/2
 **/
public interface IGroupChangeAction {
  void action(GroupInfo groupInfo);
}
